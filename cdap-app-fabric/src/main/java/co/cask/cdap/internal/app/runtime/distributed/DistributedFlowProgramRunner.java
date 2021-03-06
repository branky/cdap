/*
 * Copyright © 2014-2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.internal.app.runtime.distributed;

import co.cask.cdap.api.app.ApplicationSpecification;
import co.cask.cdap.api.common.RuntimeArguments;
import co.cask.cdap.api.flow.Flow;
import co.cask.cdap.api.flow.FlowSpecification;
import co.cask.cdap.api.flow.FlowletDefinition;
import co.cask.cdap.api.flow.flowlet.FlowletSpecification;
import co.cask.cdap.app.guice.ClusterMode;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.program.ProgramDescriptor;
import co.cask.cdap.app.queue.QueueSpecification;
import co.cask.cdap.app.queue.QueueSpecificationGenerator;
import co.cask.cdap.app.runtime.ProgramController;
import co.cask.cdap.app.runtime.ProgramOptions;
import co.cask.cdap.app.runtime.ProgramRunner;
import co.cask.cdap.app.store.Store;
import co.cask.cdap.common.ApplicationNotFoundException;
import co.cask.cdap.common.ProgramNotFoundException;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.queue.QueueName;
import co.cask.cdap.data2.transaction.queue.QueueAdmin;
import co.cask.cdap.data2.transaction.stream.StreamAdmin;
import co.cask.cdap.internal.app.queue.SimpleQueueSpecificationGenerator;
import co.cask.cdap.internal.app.runtime.SystemArguments;
import co.cask.cdap.internal.app.runtime.flow.FlowUtils;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.proto.id.ApplicationId;
import co.cask.cdap.proto.id.ProgramId;
import co.cask.cdap.security.impersonation.Impersonator;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.tephra.TransactionExecutorFactory;
import org.apache.twill.api.RunId;
import org.apache.twill.api.TwillController;
import org.apache.twill.api.TwillRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * A {@link ProgramRunner} to start a {@link Flow} program in distributed mode.
 */
public final class DistributedFlowProgramRunner extends DistributedProgramRunner
                                                implements LongRunningDistributedProgramRunner {

  private static final Logger LOG = LoggerFactory.getLogger(DistributedFlowProgramRunner.class);

  private final QueueAdmin queueAdmin;
  private final StreamAdmin streamAdmin;
  private final Store store;
  private final TransactionExecutorFactory txExecutorFactory;
  private final Impersonator impersonator;

  @Inject
  DistributedFlowProgramRunner(CConfiguration cConfig, YarnConfiguration hConf, QueueAdmin queueAdmin, Store store,
                               StreamAdmin streamAdmin, TransactionExecutorFactory txExecutorFactory,
                               Impersonator impersonator, ClusterMode clusterMode,
                               @Constants.AppFabric.ProgramRunner TwillRunner twillRunner) {
    super(cConfig, hConf, impersonator, clusterMode, twillRunner);
    this.queueAdmin = queueAdmin;
    this.streamAdmin = streamAdmin;
    this.store = store;
    this.txExecutorFactory = txExecutorFactory;
    this.impersonator = impersonator;
  }

  @Override
  protected void validateOptions(Program program, ProgramOptions options) {
    super.validateOptions(program, options);
    FlowSpecification spec = program.getApplicationSpecification().getFlows().get(program.getName());
    for (String flowlet : spec.getFlowlets().keySet()) {
      SystemArguments.validateTransactionTimeout(options.getUserArguments().asMap(), cConf, "flowlet", flowlet);
    }
  }

  @Override
  public ProgramController createProgramController(TwillController twillController,
                                                   ProgramDescriptor programDescriptor, RunId runId) {
    FlowSpecification flowSpec = programDescriptor.getSpecification();
    DistributedFlowletInstanceUpdater instanceUpdater = new DistributedFlowletInstanceUpdater(
      programDescriptor.getProgramId(), twillController, queueAdmin, streamAdmin,
      getFlowletQueues(programDescriptor.getProgramId().getParent(), flowSpec),
      txExecutorFactory, impersonator
    );
    return new FlowTwillProgramController(programDescriptor.getProgramId(), twillController,
                                          instanceUpdater, runId).startListen();
  }

  @Override
  public ProgramController createProgramController(TwillController twillController, ProgramId programId, RunId runId) {
    try {
      return createProgramController(twillController, store.loadProgram(programId), runId);
    } catch (IOException | ApplicationNotFoundException | ProgramNotFoundException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  protected void setupLaunchConfig(ProgramLaunchConfig launchConfig, Program program, ProgramOptions options,
                                   CConfiguration cConf, Configuration hConf, File tempDir) {
    // Add runnables
    Map<String, String> args = options.getUserArguments().asMap();
    FlowSpecification flowSpec = getFlowSpecification(program);

    for (Map.Entry<String, FlowletDefinition> entry  : flowSpec.getFlowlets().entrySet()) {
      FlowletDefinition flowletDefinition = entry.getValue();
      FlowletSpecification flowletSpec = flowletDefinition.getFlowletSpec();

      String flowletName = entry.getKey();
      Map<String, String> flowletArgs = RuntimeArguments.extractScope(FlowUtils.FLOWLET_SCOPE, flowletName, args);

      launchConfig.addRunnable(flowletName, new FlowletTwillRunnable(flowletName),
                               flowletDefinition.getInstances(), flowletArgs, flowletSpec.getResources());
    }
  }

  @Override
  protected void beforeLaunch(Program program, ProgramOptions options) {
    LOG.info("Configuring flowlets queues");
    FlowUtils.configureQueue(program, getFlowSpecification(program), streamAdmin, queueAdmin, txExecutorFactory);
  }

  private FlowSpecification getFlowSpecification(Program program) {
    // Extract and verify parameters
    ApplicationSpecification appSpec = program.getApplicationSpecification();
    Preconditions.checkNotNull(appSpec, "Missing application specification.");

    ProgramType processorType = program.getType();
    Preconditions.checkNotNull(processorType, "Missing processor type.");
    Preconditions.checkArgument(processorType == ProgramType.FLOW, "Only FLOW process type is supported.");

    FlowSpecification flowSpec = appSpec.getFlows().get(program.getName());
    Preconditions.checkNotNull(flowSpec, "Missing FlowSpecification for %s", program.getName());

    return flowSpec;
  }

  /**
   * Gets the queue configuration of the Flow based on the connections in the given {@link FlowSpecification}.
   */
  private Multimap<String, QueueName> getFlowletQueues(ApplicationId appId, FlowSpecification flowSpec) {
    // Generate all queues specifications
    Table<QueueSpecificationGenerator.Node, String, Set<QueueSpecification>> queueSpecs
      = new SimpleQueueSpecificationGenerator(appId).create(flowSpec);

    // For storing result from flowletId to queue.
    ImmutableSetMultimap.Builder<String, QueueName> resultBuilder = ImmutableSetMultimap.builder();

    // Loop through each flowlet
    for (Map.Entry<String, FlowletDefinition> entry : flowSpec.getFlowlets().entrySet()) {
      String flowletId = entry.getKey();
      // For each queue that the flowlet is a consumer, store the number of instances for this flowlet
      for (QueueSpecification queueSpec : Iterables.concat(queueSpecs.column(flowletId).values())) {
        resultBuilder.put(flowletId, queueSpec.getQueueName());
      }
    }
    return resultBuilder.build();
  }
}
