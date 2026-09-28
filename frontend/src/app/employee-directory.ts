import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { EmployeeApi, EmployeePage } from './employee-api';

@Component({
  selector: 'app-employee-directory',
  imports: [RouterLink, MatButtonModule, MatCardModule],
  templateUrl: './employee-directory.html',
  styleUrl: './employee-directory.scss',
})
export class EmployeeDirectory implements OnInit {
  private readonly api = inject(EmployeeApi);
  readonly data = signal<EmployeePage | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  query = '';
  country = '';
  department = '';
  level = '';
  status = 'ACTIVE';
  pageIndex = 0;

  ngOnInit(): void { this.load(); }

  search(): void {
    this.pageIndex = 0;
    this.load();
  }

  changePage(delta: number): void {
    this.pageIndex += delta;
    this.load();
  }

  formatMoney(amount: number, currency: string): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency, maximumFractionDigits: 0 })
      .format(amount);
  }

  private load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.list({ page: this.pageIndex, size: 25, query: this.query.trim(), country: this.country,
      department: this.department, level: this.level, status: this.status }).subscribe({
      next: data => { this.data.set(data); this.loading.set(false); },
      error: () => { this.error.set('Unable to load employees. Please try again.'); this.loading.set(false); },
    });
  }
}
