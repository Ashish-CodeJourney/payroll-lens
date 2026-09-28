import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AnalyticsReportView } from './analytics-report';

describe('AnalyticsReportView', () => {
  it('labels converted pay and filters the active report', async () => {
    await TestBed.configureTestingModule({
      imports: [AnalyticsReportView],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    const fixture = TestBed.createComponent(AnalyticsReportView);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    const report = { reportingCurrency: 'USD', rateDate: '2026-01-01', headcount: 2,
      annualTotalUsd: 220000, medianAnnualUsd: 110000,
      byCountry: [{ label: 'DE', headcount: 2, annualTotalUsd: 220000, medianAnnualUsd: 110000 }],
      byDepartment: [], byLevel: [], distribution: [{ label: '$100k–$149,999', headcount: 2 }] };
    http.expectOne('/api/analytics').flush(report);
    fixture.detectChanges();
    const page = fixture.nativeElement as HTMLElement;
    expect(page.textContent).toContain('$220,000');
    expect(page.textContent).toContain('2026-01-01');
    expect(page.textContent).toContain('DE');

    const department = page.querySelector<HTMLInputElement>('input[aria-label="Filter by department"]')!;
    department.value = 'Engineering';
    department.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    page.querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    const filtered = http.expectOne(request => request.url === '/api/analytics'
      && request.params.get('department') === 'Engineering');
    filtered.flush({ ...report, headcount: 1, annualTotalUsd: 110000 });
    fixture.detectChanges();
    expect(page.textContent).toContain('$110,000');
    http.verify();
  });
});
