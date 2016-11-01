/*global require, requirejs */

'use strict';

requirejs.config({
  paths: {
      'angular': ['../lib/angularjs/angular'],
      'angular-route': ['../lib/angularjs/angular-route'],
      'angular-animate' : ['../lib/angularjs/angular-animate'],
      'angular-aria' : ['../lib/angularjs/angular-aria.min'],
      'jquery' : ['https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min'],
      'angular-material' : ['../lib/angularjs/angular-material.min'],
      'angular-material-icons' : ['../lib/angularjs/angular-material-icons.min']
  },
  shim: {
      'angular': {
        exports : 'angular'
      },
      'angular-route': {
        deps: ['angular'],
        exports : 'angular'
      },
      'angular-animate': {
          deps: ['angular']
      },
      'angular-aria': {
          deps: ['angular']
      },
      'angular-material': {
         deps: ['angular-animate', 'angular-aria']
      },
      'angular-material-icons': {
          deps: ['angular-material'],
          exports : 'angular-material'
      }
  }
});

require(['angular', './controllers', './directives', './filters', './services', 'angular-route','angular-animate', 'angular-aria',
        'angular-material', 'jquery', 'angular-material-icons'],
  function(angular, controllers) {

    // Declare app level module which depends on filters, and services

    angular.module('myApp', ['myApp.filters', 'myApp.services', 'myApp.directives', 'ngRoute', 'ngMaterial']).
      config(['$routeProvider','$mdThemingProvider', function($routeProvider, $mdThemingProvider) {
        $mdThemingProvider.theme('default')
            .primaryPalette('deep-purple')
            .accentPalette('teal');

        $routeProvider.when('/view1', {templateUrl: 'partials/partial1.html', controller: controllers.MyCtrl1});
        $routeProvider.when('/view2', {templateUrl: 'partials/partial2.html', controller: controllers.MyCtrl2});
        $routeProvider.otherwise({redirectTo: '/view1'});
      }]);

    angular.bootstrap(document, ['myApp']);

});

function logMeIn() {
    alert('login was clicked');
}
