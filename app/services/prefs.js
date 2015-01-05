angular.module(PKG.name + '.services')

  .factory('myUiSettings', function (MyPrefStore) {
    return new MyPrefStore('uisettings');
  })


  .factory('MyPrefStore', function MyPrefStoreFactory($q, MyDataSource) {

    var data = new MyDataSource();

    function MyPrefStore (type) {
      // the cdap path for this preference type
      this.endpoint = '/preferences/'+type;

      // our cache of the server-side data
      this.preferences = {};

      // flag so we dont fire off multiple similar queries
      this.pending = null;
    }

    /**
     * set a preference
     * @param {string} key
     * @param {mixed} value
     * @return {promise} resolved with the response from server
     */
    MyPrefStore.prototype.set = function (key, value) {

      var deferred = $q.defer();

      this.preferences[key] = value;

      data.request(
        {
          method: 'PUT',
          _cdapPath: this.endpoint + '/properties/' + key,
          body: value
        },
        deferred.resolve
      );

      return deferred.promise;
    };


    /**
     * retrieve a preference
     * @param {string} key
     * @param {boolean} force true to bypass cache
     * @return {promise} resolved with the value
     */
    MyPrefStore.prototype.get = function (key, force) {
      if (!force && this.preferences[key]) {
        return $q.when(this.preferences[key]);
      }

      var self = this;

      if (this.pending) {
        var deferred = $q.defer();
        this.pending.promise.then(function () {
          deferred.resolve(self.preferences[key]);
        });
        return deferred.promise;
      }

      this.pending = $q.defer();

      data.request(
        {
          method: 'GET',
          _cdapPath: this.endpoint
        },
        function (res) {
          self.preferences = res;
          self.pending.resolve(res[key]);
        }
      );

      var promise = this.pending.promise;

      promise.finally(function () {
        self.pending = null;
      });

      return promise;
    };

    return MyPrefStore;
  });
