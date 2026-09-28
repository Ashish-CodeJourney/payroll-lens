import { Routes } from '@angular/router';
import { EmployeeDirectory } from './employee-directory';
import { EmployeeForm } from './employee-form';
import { AnalyticsReportView } from './analytics-report';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  { path: 'employees', component: EmployeeDirectory },
  { path: 'employees/new', component: EmployeeForm },
  { path: 'employees/:id', component: EmployeeForm },
  { path: 'reports', component: AnalyticsReportView },
];
