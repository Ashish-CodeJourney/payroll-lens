import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { AnalyticsApi, AnalyticsReport, Breakdown } from './analytics-api';

@Component({
  selector: 'app-analytics-report',
  imports: [MatButtonModule, MatCardModule],
  templateUrl: './analytics-report.html',
  styleUrl: './analytics-report.scss',
})
export class AnalyticsReportView implements OnInit {
  private readonly api = inject(AnalyticsApi);
  readonly report = signal<AnalyticsReport | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  query = '';
  country = '';
  department = '';
  level = '';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.get({ query: this.query.trim(), country: this.country, department: this.department,
      level: this.level }).subscribe({
      next: report => { this.report.set(report); this.loading.set(false); },
      error: () => { this.error.set('Unable to load report. Please try again.'); this.loading.set(false); },
    });
  }

  usd(amount: number): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD',
      maximumFractionDigits: 0 }).format(amount);
  }

  share(count: number, total: number): number {
    return total === 0 ? 0 : Math.round(count / total * 100);
  }

  rows(report: AnalyticsReport, field: 'byCountry' | 'byDepartment' | 'byLevel'): Breakdown[] {
    return report[field];
  }
}
