String.prototype.endsWith = function(suffix) {
	return this.indexOf(suffix, this.length - suffix.length) !== -1;
};
angular.module('ramlEditorApp').factory('MyFileSystem',
		function($http, $q, config, eventService) {
			var storageUrl = '/raml-jstorage/files';
			var service = {};
			var formUrlEncoder = function(obj) {
				var str = [];
				for ( var p in obj) {
					var key = encodeURIComponent(p);
					var value = encodeURIComponent(obj[p]);
					str.push([ key, value ].join("="));
				}
				return str.join("&");
			};
			service.directory = function(path) {
				var deferred = $q.defer();
				$http({
					method : 'POST',
					data : '',
					url : storageUrl + '/list',
					cache : false,
					withCredentials : false
				}).success(function(data, status, headers, config) {
					eventService.broadcast('event:notification', {
						message : 'Directory loaded.',
						expires : true
					});
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					var err = (data.error || status || 'Unknown Error');
					eventService.broadcast('event:notification', {
						message : 'Directory NOT loaded: ' + err,
						level : 'error',
						expires : false
					});
					deferred.reject.bind(deferred);
				});
				return deferred.promise;
			};
			service.save = function(path, contents) {
				var deferred = $q.defer();
				if (path.endsWith(".meta")) {
					deferred.resolve();
					return deferred.promise;
				}
				$http({
					method : 'PUT',
					data : contents,
					url : storageUrl + path,
					withCredentials : false
				}).success(function(data, status, headers, config) {
					eventService.broadcast('event:notification', {
						message : 'File saved.',
						expires : true
					});
					deferred.resolve();
				}).error(function(data, status, headers, config) {
					var err = (data.error || status || 'Unknown Error');
					eventService.broadcast('event:notification', {
						message : 'File NOT saved: ' + err,
						level : 'error',
						expires : false
					});
					deferred.reject.bind(deferred);
				});

				return deferred.promise;
			};
			service.load = function(path) {
				var deferred = $q.defer();
				if (path.endsWith(".meta")) {
					deferred.resolve("{}");
					return deferred.promise;
				}
				$http({
					method : 'GET',
					data : '',
					url : storageUrl + path,
					responseType : 'text',
					cache : false,
					withCredentials : false
				}).success(function(data, status, headers, config) {
					eventService.broadcast('event:notification', {
						message : 'File loaded.',
						expires : true
					});
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					var err = (data.error || status || 'Unknown Error');
					eventService.broadcast('event:notification', {
						message : 'File NOT loaded: ' + err,
						level : 'error',
						expires : false
					});
					deferred.reject.bind(deferred);
				});
				return deferred.promise;
			};
			service.remove = function(path) {
				var deferred = $q.defer();
				$http({
					method : 'DELETE',
					data : '',
					url : storageUrl + path,
					withCredentials : false
				}).success(function(data, status, headers, config) {
					eventService.broadcast('event:notification', {
						message : 'File removed.',
						expires : true
					});
					deferred.resolve();
				}).error(function(data, status, headers, config) {
					var err = (data.error || status || 'Unknown Error');
					eventService.broadcast('event:notification', {
						message : 'File NOT removed: ' + err,
						level : 'error',
						expires : false
					});
					deferred.reject.bind(deferred);
				});

				return deferred.promise;
			};
			service.createFolder = function(path) {
				var deferred = $q.defer();
				$http({
					method : 'POST',
					headers : {
						'Content-Type' : 'application/x-www-form-urlencoded'
					},
					data : {
						path : path
					},
					transformRequest : formUrlEncoder,
					url : storageUrl + '/createFolder',
					withCredentials : false
				}).success(function(data, status, headers, config) {
					eventService.broadcast('event:notification', {
						message : 'Folder created.',
						expires : true
					});
					deferred.resolve();
				}).error(function(data, status, headers, config) {
					var err = (data.error || status || 'Unknown Error');
					eventService.broadcast('event:notification', {
						message : 'Folder NOT created: ' + err,
						level : 'error',
						expires : false
					});
					deferred.reject.bind(deferred);
				});

				return deferred.promise;
			};
			service.rename = function(source, destination) {
				var deferred = $q.defer();
				$http({
					method : 'POST',
					headers : {
						'Content-Type' : 'application/x-www-form-urlencoded'
					},
					data : {
						source : source,
						destination : destination
					},
					transformRequest : formUrlEncoder,
					url : storageUrl + '/rename',
					withCredentials : false
				}).success(function(data, status, headers, config) {
					eventService.broadcast('event:notification', {
						message : 'File renamed.',
						expires : true
					});
					deferred.resolve();
				}).error(function(data, status, headers, config) {
					var err = (data.error || status || 'Unknown Error');
					eventService.broadcast('event:notification', {
						message : 'File NOT renamed: ' + err,
						level : 'error',
						expires : false
					});
					deferred.reject.bind(deferred);
				});

				return deferred.promise;
			};
			return service;
		}).run(function(MyFileSystem, config, eventService) {
	// Set MyFileSystem as the filesystem to use
	config.set('fsFactory', 'MyFileSystem');

	// In case you want to send notifications to the user
	// (for instance, that he must login to save).
	// The expires flags means whether
	// it should be hidden after a period of time or the
	// user should dismiss it manually.
	// eventService.broadcast('event:notification', {
	// message : 'File saved.', expires : true
	// });
});
