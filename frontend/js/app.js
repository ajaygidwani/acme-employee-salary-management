/**
 * ACME Employee & Salary Management
 * AngularJS Application Module
 */

(function () {
  'use strict';

  angular.module('employeeApp', [])
    .value('apiConfig', {
      baseUrl: '/api/v1'
    });
})();
