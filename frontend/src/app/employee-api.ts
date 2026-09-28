import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface Employee {
  id: number;
  employeeNumber: string;
  fullName: string;
  email: string;
  countryCode: string;
  department: string;
  jobTitle: string;
  jobLevel: string;
  annualSalary: number;
  currencyCode: string;
  archived: boolean;
}

export interface EmployeePage {
  items: Employee[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface EmployeeFilters {
  page: number;
  size: number;
  query?: string;
  country?: string;
  department?: string;
  level?: string;
  status?: string;
}

@Injectable({ providedIn: 'root' })
export class EmployeeApi {
  private readonly http = inject(HttpClient);

  list(filters: EmployeeFilters): Observable<EmployeePage> {
    let params = new HttpParams().set('page', filters.page).set('size', filters.size);
    for (const key of ['query', 'country', 'department', 'level', 'status'] as const) {
      const value = filters[key];
      if (value) params = params.set(key, value);
    }
    return this.http.get<EmployeePage>('/api/employees', { params });
  }
}
