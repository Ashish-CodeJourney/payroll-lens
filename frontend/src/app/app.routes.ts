import { Routes } from '@angular/router';
import { EmployeeDirectory } from './employee-directory';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  { path: 'employees', component: EmployeeDirectory },
];
