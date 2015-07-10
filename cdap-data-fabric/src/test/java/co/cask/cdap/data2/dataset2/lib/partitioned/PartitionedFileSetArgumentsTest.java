/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.data2.dataset2.lib.partitioned;

import co.cask.cdap.api.dataset.lib.FileSetArguments;
import co.cask.cdap.api.dataset.lib.Partition;
import co.cask.cdap.api.dataset.lib.PartitionFilter;
import co.cask.cdap.api.dataset.lib.PartitionKey;
import co.cask.cdap.api.dataset.lib.PartitionedFileSetArguments;
import co.cask.cdap.api.dataset.lib.Partitioning;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class PartitionedFileSetArgumentsTest {

  private static final Partitioning PARTITIONING = Partitioning.builder()
    .addStringField("s")
    .addIntField("i")
    .addLongField("l")
    .build();

  @Test
  public void testSetGetOutputPartitionKey() throws Exception {
    Map<String, String> arguments = Maps.newHashMap();
    PartitionKey key = PartitionKey.builder()
      .addIntField("i", 42)
      .addLongField("l", 17L)
      .addStringField("s", "x")
      .build();
    PartitionedFileSetArguments.setOutputPartitionKey(arguments, key);
    Assert.assertEquals(key, PartitionedFileSetArguments.getOutputPartitionKey(arguments, PARTITIONING));
  }

  @Test
  public void testSetGetOutputPartitionMetadata() throws Exception {
    Map<String, String> arguments = Maps.newHashMap();
    Map<String, String> metadata = ImmutableMap.of("metakey1", "value1",
                                                   "metaKey2", "value3");
    PartitionedFileSetArguments.setOutputPartitionMetadata(arguments, metadata);
    Assert.assertEquals(metadata, PartitionedFileSetArguments.getOutputPartitionMetadata(arguments));

    // test also with empty metadata
    arguments = Maps.newHashMap();
    PartitionedFileSetArguments.setOutputPartitionMetadata(arguments, Collections.<String, String>emptyMap());
    Assert.assertEquals(Collections.<String, String>emptyMap(),
                        PartitionedFileSetArguments.getOutputPartitionMetadata(arguments));
  }

  @Test
  public void testSetGetInputPartitionFilter() throws Exception {
    Map<String, String> arguments = Maps.newHashMap();
    PartitionFilter filter = PartitionFilter.builder()
      .addValueCondition("i", 42)
      .addValueCondition("l", 17L)
      .addValueCondition("s", "x")
      .build();
    PartitionedFileSetArguments.setInputPartitionFilter(arguments, filter);
    Assert.assertEquals(filter, PartitionedFileSetArguments.getInputPartitionFilter(arguments, PARTITIONING));
  }


  @Test
  public void testGetPartitionPaths() throws Exception {
    Map<String, String> arguments = Maps.newHashMap();

    Collection<String> relativePaths = Lists.newArrayList("path1", "relative/path.part100", "some\\ other*path");
    List<Partition> partitions = Lists.newArrayList();
    for (String relativePath : relativePaths) {
      BasicPartition basicPartition = new BasicPartition(null, relativePath, null);
      PartitionedFileSetArguments.addPartition(arguments, basicPartition);
      // add the partitions to a list to also test the addPartitions(Map, Iterator) method below
      partitions.add(basicPartition);
    }
    Assert.assertEquals(relativePaths, FileSetArguments.getInputPaths(arguments));

    arguments = Maps.newHashMap();
    PartitionedFileSetArguments.addPartitions(arguments, partitions.iterator());
    Assert.assertEquals(relativePaths, FileSetArguments.getInputPaths(arguments));

  }
}
