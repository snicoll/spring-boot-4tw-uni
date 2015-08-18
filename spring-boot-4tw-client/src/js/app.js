angular.module('diffApp', ['ngRoute', 'ui.bootstrap'])

    .config(['$locationProvider', function ($locationProvider) {
        $locationProvider.html5Mode(true).hashPrefix('!');
    }])
    .factory('jQuery', ['$window', function ($window) {
        return $window.jQuery;
    }])
    .constant('springBootVersionURL', '/springboot/versions.json')
    .config(['$provide', function ($provide) {
        $provide.value("apiRoot", $("head > base").attr("href"));
    }])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/', {
                controller: 'FormController as form',
                templateUrl: 'diffListTemplate'
            })
            .when('/compare/:fromVersion/:toVersion', {
                controller: 'DiffController as diff',
                templateUrl: 'diffListTemplate'
            });
    }])
    .service('ConfigDiff', ['$http', 'apiRoot', 'springBootVersionURL', '$q', '$timeout',
        function ($http, apiRoot, springBootVersionURL, $q, $timeout) {
            var fetchDiff = function (fromVersion, toVersion) {

                var deferred = $q.defer();
                $http.get(apiRoot + "diff/" + fromVersion + "/" + toVersion + "/")
                    .success(function (data) {
                        deferred.resolve(data);
                    })
                    .error(function (data, status, headers, config) {
                        deferred.reject(data);
                    });
                return deferred.promise;
            };

            var fetchBootVersions = function () {
                return $http.get(springBootVersionURL);
            };

            return {
                fetchDiff: fetchDiff,
                fetchBootVersions: fetchBootVersions
            };
        }])
    .controller('FormController', ['$scope', '$location', 'jQuery', 'ConfigDiff',
        function ($scope, $location, $, ConfigDiff) {
            $scope.bootVersions = ConfigDiff.fetchBootVersions()
                .then(function (result) {
                    $scope.bootVersions = result.data;
                });
            $scope.compare = function (fromVersion, toVersion) {
                $location.url("/compare/" + fromVersion + "/" + toVersion + "/");
            };
        }])
    .controller('DiffController', ['$scope', '$routeParams', 'ConfigDiff', '$location', '$anchorScroll', 'jQuery',
        function ($scope, $routeParams, ConfigDiff, $location, $anchorScroll, $) {

            $('body').scrollspy({target: '.navbar-side'});
            $('.navbar-side').affix({offset: {top: 10}});
            var fromVersion = $routeParams.fromVersion;
            var toVersion = $routeParams.toVersion;
            $scope.loading = true;

            ConfigDiff.fetchDiff(fromVersion, toVersion)
                .then(function (data) {
                    $scope.diffs = {
                        groups: data.groups,
                        fromVersion: fromVersion,
                        toVersion: toVersion
                    };
                    $scope.loading = false;
                },
                function (error) {
                    $scope.exception = error;
                    $scope.loading = false;
                });
            $scope.bootVersions = ConfigDiff.fetchBootVersions()
                .then(function (result) {
                    $scope.bootVersions = result.data;
                });
            $scope.compare = function (fromVersion, toVersion) {
                $location.url("/compare/" + fromVersion + "/" + toVersion + "/");
            };
            $scope.fromVersion = fromVersion;
            $scope.toVersion = toVersion;
            $scope.gotoAnchor = function (e, group) {
                $anchorScroll(group.id.replace(/\./g, "-"));
                e.preventDefault();
            };
        }])

    .filter('anchor', function () {
        return function (input) {
            return input ? input.replace(/\./g, "-") : "";
        };
    });