import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface Breakdown {
  label: string;
  headcount: number;
  annualTotalUsd: number;
  medianAnnualUsd: number;
}

export interface DistributionBand {
  label: string;
  headcount: number;
}

export interface AnalyticsReport {
  reportingCurrency: string;
  rateDate: string;
  headcount: number;
  annualTotalUsd: number;
  medianAnnualUsd: number;
  byCountry: Breakdown[];
  byDepartment: Breakdown[];
  byLevel: Breakdown[];
  distribution: DistributionBand[];
}

export interface ReportFilters {
  query?: string;
  country?: string;
  department?: string;
  level?: string;
}

@Injectable({ providedIn: 'root' })
export class AnalyticsApi {
  private readonly http = inject(HttpClient);

  get(filters: ReportFilters): Observable<AnalyticsReport> {
    let params = new HttpParams();
    for (const key of ['query', 'country', 'department', 'level'] as const) {
      const value = filters[key];
      if (value) params = params.set(key, value);
    }
    return this.http.get<AnalyticsReport>('/api/analytics', { params });
  }
}
