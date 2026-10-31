/**
 * Employee Service
 * Handles all HTTP interactions with /api/v1/employees
 */

(function () {
  'use strict';

  angular.module('employeeApp')
    .factory('EmployeeService', ['$http', 'apiConfig', function ($http, apiConfig) {

      function getBaseUrl() {
        return apiConfig.baseUrl + '/employees';
      }

      var statuses = ['ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED'];

      return {
        getStatuses: function () {
          return statuses;
        },

        search: function (params) {
          var cleanParams = {
            page: params.page || 0,
            size: params.size || 10,
            sort: params.sort || 'id,asc'
          };

          if (params.name && params.name.trim()) {
            cleanParams.name = params.name.trim();
          }
          if (params.employeeCode && params.employeeCode.trim()) {
            cleanParams.employeeCode = params.employeeCode.trim();
          }
          if (params.department && params.department.trim()) {
            cleanParams.department = params.department.trim();
          }
          if (params.countryCode && params.countryCode.trim()) {
            cleanParams.countryCode = params.countryCode.trim().toUpperCase();
          }

          return $http.get(getBaseUrl(), { params: cleanParams });
        },

        create: function (employeeData) {
          return $http.post(getBaseUrl(), employeeData);
        },

        update: function (id, employeeData) {
          return $http.put(getBaseUrl() + '/' + id, employeeData);
        },

        delete: function (id) {
          return $http.delete(getBaseUrl() + '/' + id);
        }
      };
    }]);
})();
