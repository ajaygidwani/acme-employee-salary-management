/**
 * Main Controller for Employee & Salary Management
 */

(function () {
  'use strict';

  angular.module('employeeApp')
    .controller('MainController', [
      '$scope',
      '$timeout',
      'EmployeeService',
      'SalaryService',
      'apiConfig',
      function ($scope, $timeout, EmployeeService, SalaryService, apiConfig) {

        // --- State Models ---
        $scope.employees = [];
        $scope.selectedEmployee = null;
        $scope.salaryHistory = [];
        $scope.employmentStatuses = EmployeeService.getStatuses();

        $scope.pagination = {
          page: 0,
          size: 10,
          totalPages: 0,
          totalElements: 0,
          first: true,
          last: true
        };

        $scope.filters = {
          name: '',
          employeeCode: '',
          department: '',
          countryCode: ''
        };

        $scope.loading = {
          employees: false,
          salaries: false,
          saveEmployee: false,
          saveSalary: false,
          deleteEmployee: false
        };

        $scope.backendStatus = 'checking'; // 'connected' | 'disconnected' | 'checking'
        $scope.backendErrorDetails = '';
        $scope.customApiUrl = apiConfig.baseUrl;

        $scope.alert = null;
        var alertTimer = null;

        // Modals
        $scope.modal = {
          employee: false,
          isEdit: false,
          salary: false,
          confirmDelete: false,
          settings: false
        };

        // Form Models
        $scope.employeeForm = {
          id: null,
          employeeCode: '',
          name: '',
          email: '',
          department: '',
          designation: '',
          countryCode: '',
          employmentStatus: 'ACTIVE'
        };

        $scope.salaryForm = {
          amount: null,
          currency: 'USD',
          effectiveDateVal: new Date()
        };

        $scope.employeeToDelete = null;

        // --- Helper: Format Date YYYY-MM-DD ---
        function formatDateToIso(d) {
          if (!d) return '';
          if (typeof d === 'string') return d;
          if (d instanceof Date) {
            var month = ('0' + (d.getMonth() + 1)).slice(-2);
            var day = ('0' + d.getDate()).slice(-2);
            return d.getFullYear() + '-' + month + '-' + day;
          }
          return '';
        }

        // --- Alert Helper ---
        $scope.showAlert = function (type, message) {
          $scope.alert = {
            type: type, // 'success' | 'danger' | 'warning' | 'info'
            message: message
          };
          if (alertTimer) {
            $timeout.cancel(alertTimer);
          }
          alertTimer = $timeout(function () {
            $scope.alert = null;
          }, 6000);
        };

        $scope.dismissAlert = function () {
          $scope.alert = null;
          if (alertTimer) {
            $timeout.cancel(alertTimer);
          }
        };

        // Extract error message from backend ApiErrorResponse
        function extractErrorMessage(errorResponse, fallback) {
          if (errorResponse && errorResponse.data) {
            if (typeof errorResponse.data === 'string') {
              return errorResponse.data;
            }
            if (errorResponse.data.message) {
              return errorResponse.data.message;
            }
            if (errorResponse.data.error) {
              return errorResponse.data.error;
            }
          }
          if (errorResponse && errorResponse.status === -1) {
            return 'Network error: Cannot reach the backend. Ensure Spring Boot is running on port 8080 and the dev proxy is active.';
          }
          if (errorResponse && errorResponse.status === 502) {
            return 'Proxy error: Spring Boot is not reachable at ' + apiConfig.baseUrl + '. Please start the backend.';
          }
          return fallback || 'An unexpected error occurred.';
        }

        // --- Backend Health Check ---
        $scope.checkBackendHealth = function () {
          $scope.backendStatus = 'checking';
          $scope.backendErrorDetails = '';
          EmployeeService.search({ page: 0, size: 1 })
            .then(function () {
              $scope.backendStatus = 'connected';
            })
            .catch(function (err) {
              $scope.backendStatus = 'disconnected';
              $scope.backendErrorDetails = extractErrorMessage(err, 'Backend unreachable');
            });
        };

        // --- Save Custom API Settings ---
        $scope.saveApiConfig = function () {
          if ($scope.customApiUrl && $scope.customApiUrl.trim()) {
            apiConfig.baseUrl = $scope.customApiUrl.trim();
            $scope.closeModal('settings');
            $scope.showAlert('info', 'API Base URL updated to ' + apiConfig.baseUrl);
            $scope.checkBackendHealth();
            $scope.loadEmployees(0);
          }
        };

        // --- Load Employees ---
        $scope.loadEmployees = function (pageNumber) {
          if (pageNumber !== undefined && pageNumber !== null) {
            $scope.pagination.page = pageNumber;
          }

          $scope.loading.employees = true;
          var searchParams = {
            page: $scope.pagination.page,
            size: $scope.pagination.size,
            name: $scope.filters.name,
            employeeCode: $scope.filters.employeeCode,
            department: $scope.filters.department,
            countryCode: $scope.filters.countryCode
          };

          EmployeeService.search(searchParams)
            .then(function (response) {
              var data = response.data;
              $scope.backendStatus = 'connected';
              $scope.employees = data.content || [];
              $scope.pagination.totalPages = data.totalPages || 0;
              $scope.pagination.totalElements = data.totalElements || 0;
              $scope.pagination.first = data.first !== undefined ? data.first : ($scope.pagination.page === 0);
              $scope.pagination.last = data.last !== undefined ? data.last : ($scope.pagination.page >= $scope.pagination.totalPages - 1);

              // If previously selected employee is still in list, re-select to keep fresh
              if ($scope.selectedEmployee) {
                var found = $scope.employees.find(function (e) {
                  return e.id === $scope.selectedEmployee.id;
                });
                if (found) {
                  $scope.selectedEmployee = found;
                }
              }
            })
            .catch(function (err) {
              $scope.backendStatus = 'disconnected';
              $scope.showAlert('danger', 'Failed to load employees: ' + extractErrorMessage(err));
            })
            .finally(function () {
              $scope.loading.employees = false;
            });
        };

        // Search Filters
        $scope.applyFilters = function () {
          $scope.loadEmployees(0);
        };

        $scope.resetFilters = function () {
          $scope.filters = {
            name: '',
            employeeCode: '',
            department: '',
            countryCode: ''
          };
          $scope.loadEmployees(0);
        };

        // --- Select Employee & Load Salary History ---
        $scope.selectEmployee = function (emp) {
          $scope.selectedEmployee = emp;
          $scope.loadSalaryHistory(emp.id);
        };

        $scope.clearSelectedEmployee = function () {
          $scope.selectedEmployee = null;
          $scope.salaryHistory = [];
        };

        $scope.loadSalaryHistory = function (employeeId) {
          $scope.loading.salaries = true;
          SalaryService.getSalaryHistory(employeeId)
            .then(function (response) {
              $scope.salaryHistory = response.data || [];
            })
            .catch(function (err) {
              $scope.showAlert('warning', 'Could not load salary history: ' + extractErrorMessage(err));
            })
            .finally(function () {
              $scope.loading.salaries = false;
            });
        };

        // --- Modal Control ---
        $scope.openModal = function (modalName) {
          $scope.modal[modalName] = true;
        };

        $scope.closeModal = function (modalName) {
          $scope.modal[modalName] = false;
        };

        // --- Create / Edit Employee ---
        $scope.openCreateEmployeeModal = function () {
          $scope.modal.isEdit = false;
          $scope.employeeForm = {
            id: null,
            employeeCode: '',
            name: '',
            email: '',
            department: '',
            designation: '',
            countryCode: '',
            employmentStatus: 'ACTIVE'
          };
          $scope.openModal('employee');
        };

        $scope.openEditEmployeeModal = function (emp, $event) {
          if ($event) {
            $event.stopPropagation();
          }
          $scope.modal.isEdit = true;
          $scope.employeeForm = {
            id: emp.id,
            employeeCode: emp.employeeCode,
            name: emp.name,
            email: emp.email,
            department: emp.department,
            designation: emp.designation,
            countryCode: emp.countryCode,
            employmentStatus: emp.employmentStatus
          };
          $scope.openModal('employee');
        };

        $scope.saveEmployee = function (form) {
          if (form && form.$invalid) {
            $scope.showAlert('warning', 'Please fill all required fields properly.');
            return;
          }

          $scope.loading.saveEmployee = true;
          var payload = {
            employeeCode: $scope.employeeForm.employeeCode.trim(),
            name: $scope.employeeForm.name.trim(),
            email: $scope.employeeForm.email.trim(),
            department: $scope.employeeForm.department.trim(),
            designation: $scope.employeeForm.designation.trim(),
            countryCode: $scope.employeeForm.countryCode.trim().toUpperCase(),
            employmentStatus: $scope.employeeForm.employmentStatus
          };

          var promise;
          if ($scope.modal.isEdit && $scope.employeeForm.id) {
            promise = EmployeeService.update($scope.employeeForm.id, payload);
          } else {
            promise = EmployeeService.create(payload);
          }

          promise
            .then(function (response) {
              $scope.closeModal('employee');
              var action = $scope.modal.isEdit ? 'updated' : 'created';
              $scope.showAlert('success', 'Employee ' + response.data.name + ' (' + response.data.employeeCode + ') ' + action + ' successfully!');

              if ($scope.selectedEmployee && $scope.selectedEmployee.id === response.data.id) {
                $scope.selectedEmployee = response.data;
              }

              $scope.loadEmployees($scope.modal.isEdit ? $scope.pagination.page : 0);
            })
            .catch(function (err) {
              $scope.showAlert('danger', 'Save failed: ' + extractErrorMessage(err));
            })
            .finally(function () {
              $scope.loading.saveEmployee = false;
            });
        };

        // --- Delete Employee ---
        $scope.confirmDeleteEmployee = function (emp, $event) {
          if ($event) {
            $event.stopPropagation();
          }
          $scope.employeeToDelete = emp;
          $scope.openModal('confirmDelete');
        };

        $scope.executeDeleteEmployee = function () {
          if (!$scope.employeeToDelete) return;

          $scope.loading.deleteEmployee = true;
          var empName = $scope.employeeToDelete.name;
          var empId = $scope.employeeToDelete.id;

          EmployeeService.delete(empId)
            .then(function () {
              $scope.closeModal('confirmDelete');
              $scope.showAlert('success', 'Employee ' + empName + ' deleted successfully.');
              if ($scope.selectedEmployee && $scope.selectedEmployee.id === empId) {
                $scope.clearSelectedEmployee();
              }
              $scope.loadEmployees($scope.pagination.page);
            })
            .catch(function (err) {
              $scope.showAlert('danger', 'Delete failed: ' + extractErrorMessage(err));
            })
            .finally(function () {
              $scope.loading.deleteEmployee = false;
              $scope.employeeToDelete = null;
            });
        };

        // --- Add Salary ---
        $scope.openAddSalaryModal = function () {
          if (!$scope.selectedEmployee) {
            $scope.showAlert('warning', 'Please select an employee first.');
            return;
          }
          $scope.salaryForm = {
            amount: null,
            currency: 'USD',
            effectiveDateVal: new Date()
          };
          $scope.openModal('salary');
        };

        $scope.saveSalary = function (form) {
          if (!$scope.selectedEmployee) return;

          if (form && form.$invalid) {
            $scope.showAlert('warning', 'Please provide a valid salary amount, 3-letter currency code, and effective date.');
            return;
          }

          var formattedDate = formatDateToIso($scope.salaryForm.effectiveDateVal);
          if (!formattedDate) {
            $scope.showAlert('warning', 'Please enter a valid effective date.');
            return;
          }

          $scope.loading.saveSalary = true;
          var payload = {
            amount: $scope.salaryForm.amount,
            currency: $scope.salaryForm.currency,
            effectiveDate: formattedDate
          };

          SalaryService.addSalary($scope.selectedEmployee.id, payload)
            .then(function (response) {
              $scope.closeModal('salary');
              $scope.showAlert('success', 'Salary of ' + response.data.currency + ' ' + response.data.amount + ' added successfully.');
              $scope.loadSalaryHistory($scope.selectedEmployee.id);
            })
            .catch(function (err) {
              $scope.showAlert('danger', 'Failed to add salary: ' + extractErrorMessage(err));
            })
            .finally(function () {
              $scope.loading.saveSalary = false;
            });
        };

        // --- Quick Demo Autofill Helpers ---
        var demoIndex = 100 + Math.floor(Math.random() * 800);
        var sampleDepartments = ['Engineering', 'Product', 'Marketing', 'Human Resources', 'Finance', 'Design'];
        var sampleDesignations = ['Software Engineer', 'Senior Lead', 'Product Manager', 'Data Analyst', 'DevOps Specialist', 'HR Partner'];
        var sampleCountries = ['US', 'IN', 'GB', 'CA', 'DE', 'SG'];

        $scope.quickFillEmployee = function () {
          demoIndex++;
          var randDept = sampleDepartments[demoIndex % sampleDepartments.length];
          var randDesig = sampleDesignations[demoIndex % sampleDesignations.length];
          var randCountry = sampleCountries[demoIndex % sampleCountries.length];

          $scope.employeeForm = {
            id: $scope.modal.isEdit ? $scope.employeeForm.id : null,
            employeeCode: 'EMP-' + demoIndex,
            name: 'Demo Person ' + demoIndex,
            email: 'demo' + demoIndex + '@acme.com',
            department: randDept,
            designation: randDesig,
            countryCode: randCountry,
            employmentStatus: 'ACTIVE'
          };
        };

        $scope.quickFillSalary = function () {
          var amounts = [65000, 78000, 92000, 110000, 125000, 140000];
          var currencies = ['USD', 'EUR', 'INR', 'GBP'];
          var randAmt = amounts[Math.floor(Math.random() * amounts.length)];
          var randCur = currencies[Math.floor(Math.random() * currencies.length)];

          $scope.salaryForm = {
            amount: randAmt,
            currency: randCur,
            effectiveDateVal: new Date()
          };
        };

        // --- Pagination Helpers ---
        $scope.getPageNumbers = function () {
          var pages = [];
          var total = $scope.pagination.totalPages;
          var current = $scope.pagination.page;

          var start = Math.max(0, current - 2);
          var end = Math.min(total, start + 5);
          if (end - start < 5) {
            start = Math.max(0, end - 5);
          }

          for (var i = start; i < end; i++) {
            pages.push(i);
          }
          return pages;
        };

        // --- Initialization ---
        $scope.checkBackendHealth();
        $scope.loadEmployees(0);

      }
    ]);
})();
