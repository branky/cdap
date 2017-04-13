/*
 * Copyright © 2016 Cask Data, Inc.
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

angular.module(PKG.name + '.feature.hydrator')
  .controller('HydratorPlusPlusListController', function($scope, myPipelineApi, $stateParams, GLOBALS, mySettings, $state, myHelpers, myWorkFlowApi, myWorkersApi, MyCDAPDataSource, myAppsApi, myAlertOnValium, myLoadingService, mySparkApi, $interval, moment, MyPipelineStatusMapper) {
    var dataSrc = new MyCDAPDataSource($scope);
    var vm = this;
    vm.$interval = $interval;
    vm.moment = moment;
    vm.runningPolls = [];
    vm.pipelineList = [];
    vm.pipelineListLoaded = false;
    vm.MyPipelineStatusMapper = MyPipelineStatusMapper;
    var eventEmitter = window.CaskCommon.ee(window.CaskCommon.ee);
    vm.statusCount = {
      running: {
        batch: 0,
        realtime: 0
      },
      draft: 0
    };
    vm.GLOBALS = GLOBALS;
    var realtime = [],
        batch = [];
    var statusMap = {};
    getPipelines();

    const checkForValidPage = (pageNumber) => {
      return (
        !Number.isNaN(pageNumber) &&
        Array.isArray(vm.pipelineList) &&
        Math.floor(vm.pipelineList.length) >= pageNumber
      );
    };

    const setCurrentPage = () => {
      let pageNumber = parseInt($stateParams.page, 10);
      if (pageNumber&& checkForValidPage(pageNumber)) {
        vm.currentPage = pageNumber;
      } else {
        vm.currentPage = 1;
      }
      vm.goToPage();
    };

    vm.reloadState = function() {
      $state.reload();
    };
    eventEmitter.on(window.CaskCommon.globalEvents.PUBLISHPIPELINE, vm.reloadState);

    vm.goToPage = function () {
      if (!vm.currentPage) {
        return;
      }
      $stateParams.page = vm.currentPage;
      vm.pipelineListLoaded = true;
      $state.go('hydrator.list', $stateParams, {notify: false});
    };

    vm.deleteDraft = function(draftId) {
      myLoadingService.showLoadingIcon()
      .then(function(){
        let draftName;
        mySettings.get('hydratorDrafts')
          .then(function(res){
            let draft = myHelpers.objectQuery(res, $stateParams.namespace, draftId);
            if(draft){
              draftName = draft.name;
              delete res[$stateParams.namespace][draftId];
            }
            return mySettings.set('hydratorDrafts', res);
          })
          .then(
              function success() {
                myLoadingService.hideLoadingIconImmediate();
                $state.reload()
                  .then(function() {
                    myAlertOnValium.show({
                      type: 'success',
                      content: 'Pipeline draft ' + draftName + ' deleted successfully'
                    });
                  });
              },
              function error() {
                myLoadingService.hideLoadingIconImmediate();
                $state.reload()
                  .then(function() {
                    myAlertOnValium.show({
                      type: 'danger',
                      content: 'Pipeline draft ' + draftName + ' delete failed'
                    });
                  });
              });
      });
    };

    vm.deleteApp = function (appId) {
      myLoadingService.showLoadingIcon()
      .then(function(){
        var deleteParams = {
          namespace: $state.params.namespace,
          appId: appId,
          scope: $scope
        };
        return myAppsApi.delete(deleteParams)
          .$promise;
      })
      .then(function success () {
        myLoadingService.hideLoadingIconImmediate();
        $state.reload()
          .then(function() {
            myAlertOnValium.show({
              type: 'success',
              content: 'Pipeline ' + appId + ' deleted successfully'
            });
          });
      }, function error () {
        myLoadingService.hideLoadingIconImmediate();
        $state.reload()
          .then(function() {
            myAlertOnValium.show({
              type: 'danger',
              content:  'Pipeline ' + appId + ' delete failed'
            });
          });
      });
    };

    $scope.$on('$destroy', function() {
      eventEmitter.off(window.CaskCommon.globalEvents.PUBLISHPIPELINE, vm.reloadState);
      destroyAllTimers();
      stopPollingAll();
    });

    function fetchRunsInfo(app) {
      var params = {
        namespace: $state.params.namespace,
        appId: app.id,
        scope: $scope
      };

      var api;

      if (app.artifact.name === GLOBALS.etlBatch || app.artifact.name === GLOBALS.etlDataPipeline) {
        api = myWorkFlowApi;

        var workflowId = app.artifact.name === GLOBALS.etlDataPipeline ? 'DataPipelineWorkflow' : 'ETLWorkflow';
        params.workflowId = workflowId;

        batch.push({
          appId: app.id,
          programType: 'Workflow',
          programId: workflowId
        });
      } else if (app.artifact.name === GLOBALS.etlDataStreams) {
        api = mySparkApi;

        realtime.push({
          appId: app.id,
          programType: 'Spark',
          programId: 'DataStreamsSparkStreaming'
        });

      } else {
        api = myWorkersApi;
        params.workerId = 'ETLWorker';

        realtime.push({
          appId: app.id,
          programType: 'Worker',
          programId: 'ETLWorker'
        });
      }

      api.runs(params)
        .$promise
        .then(function (runs) {
          if (runs.length) {
            app._stats.numRuns = runs.length;
            app._stats.lastStartTime = runs.length > 0 ? runs[0].start : 'N/A';
            var currentRun = runs[0];
            setDurationTimers(app, currentRun);
            app._latest = currentRun;
            statusMap[app.id] = vm.MyPipelineStatusMapper.lookupDisplayStatus(app._latest.status);
          } else {
            statusMap[app.id] = vm.MyPipelineStatusMapper.lookupDisplayStatus('SUSPENDED');
          }
          updateStatusAppObject();
        });
    }

    function getPipelines() {
      myPipelineApi.list({
        namespace: $stateParams.namespace
      })
        .$promise
        .then(function success(res) {
          vm.pipelineList = res;

          fetchDrafts().then(setCurrentPage);

          angular.forEach(vm.pipelineList, function (app) {
            app._stats = {};

            fetchRunsInfo(app);
          });

          fetchStatus();
          fetchWorkflowNextRunTimes();
        });
    }

    /**
     * Gets the next workflow run times. This must be called after batch objects have been created.
     */
    function fetchWorkflowNextRunTimes() {
      batch.forEach(function (batchParams) {
        dataSrc.request({
          _cdapNsPath: '/apps/' + batchParams.appId +'/workflows/' + batchParams.programId +'/nextruntime',
        })
        .then(function (res) {
          if (res && res.length) {
            vm.pipelineList.forEach(function (app) {
              if (app.id === batchParams.appId) {
                app._stats.nextRun = res[0].time;
              }
            });
          }
        });
      });
    }

    function setDurationTimers(app, run) {
      if (run.status === 'RUNNING') {

        if (!app.pipelineDurationTimer) {
          app.pipelineDurationTimer = vm.$interval(() => {
            if (run.end) {
              let endDuration = run.end - run.start;
              app.duration = typeof run.end === 'number' ? vm.moment.utc(endDuration * 1000).format('HH:mm:ss') : 'N/A';
            } else {
              let runningDuration = new Date().getTime() - (run.start * 1000);
              app.duration = vm.moment.utc(runningDuration).format('HH:mm:ss');
            }
          }, 1000);
        }
      } else {
        let lastRunDuration = run.end - run.start;

        let setInitialTimer = new Date().getTime() - (run.start * 1000);
        app.duration = typeof run.end === 'number' ?
                            vm.moment.utc(lastRunDuration * 1000).format('HH:mm:ss') :
                            vm.moment.utc(setInitialTimer).format('HH:mm:ss');
      }
    }

    function fetchStatus() {
      // fetching ETL Batch statuses
      dataSrc.poll({
        _cdapNsPath: '/status',
        method: 'POST',
        interval: 2000,
        body: batch
      })
      .then(function (res) {
        vm.runningPolls.push(res.__pollId__);
        vm.statusCount.running.batch = 0;
        angular.forEach(res, function (app) {
          if (app.status === 'RUNNING') {
            vm.statusCount.running.batch++;
          }
        });

        updateStatusAppObject();
      }); // end of ETL Batch

      //fetching ETL Realtime statuses
      dataSrc.poll({
        _cdapNsPath: '/status',
        method: 'POST',
        interval: 2000,
        body: realtime
      })
      .then(function (res) {
        vm.runningPolls.push(res.__pollId__);
        vm.statusCount.running.realtime = 0;
        angular.forEach(res, function (app) {
          if (app.status === 'RUNNING') {
            statusMap[app.appId] = vm.MyPipelineStatusMapper.lookupDisplayStatus(app.status);
            vm.statusCount.running.realtime++;
          }
        });

        updateStatusAppObject();
      });
    }

    function updateStatusAppObject() {
      angular.forEach(vm.pipelineList, function (app) {
        app._status = app._status || statusMap[app.id];
        if (app._status === 'COMPLETED' || app._status === 'KILLED') {
          destroyTimerForApp(app);
        }
        app.displayStatus = app._status;
      });
    }

    function fetchDrafts() {
      return mySettings.get('hydratorDrafts', true)
        .then(function(res) {
          let draftsList = myHelpers.objectQuery(res, $stateParams.namespace);
          if (!angular.isObject(draftsList)) {
            return;
          }
          if (Object.keys(draftsList).length) {
            angular.forEach(res[$stateParams.namespace], function(value, key) {
              vm.statusCount.draft++;
              vm.pipelineList.push({
                isDraft: true,
                name: value.name,
                id: (value.__ui__  && value.__ui__.draftId) ? value.__ui__.draftId : key,
                artifact: value.artifact,
                description: myHelpers.objectQuery(value, 'description'),
                _status: 'Draft',
                _stats: {
                  numRuns: 'N/A',
                  lastStartTime: 'N/A'
                }
              });
            });
          }
        });
    }

    function destroyTimerForApp(app) {
      if (app.pipelineDurationTimer) {
        vm.$interval.cancel(app.pipelineDurationTimer);
        app.pipelineDurationTimer = null;
      }
    }

    function destroyAllTimers() {
      angular.forEach(vm.pipelineList, function (app) {
        destroyTimerForApp(app);
      });
    }

    function stopPollingAll() {
      vm.runningPolls.forEach(function (pollId) {
        dataSrc.stopPoll(pollId);
      });
    }
  });
