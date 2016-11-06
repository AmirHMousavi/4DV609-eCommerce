/*global define */

'use strict';

define(['angular'], function(angular) {

/* Directives */

angular.module('myApp.directives', [])
  .directive('appVersion', ['version', function(version) {
      return function(scope, elm, attrs) {
      elm.text(version);
    };
  }])
  .directive('itemCard', function(){
      return {
        restrict:'E',
        templateUrl:'views/item-card.html'
    };
  });

});