import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { EmployeeDirectory } from './employee-directory';

describe('EmployeeDirectory', () => {
  it('loads a page and searches without fetching all employees', async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeDirectory],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    const fixture = TestBed.createComponent(EmployeeDirectory);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    const first = http.expectOne(request => request.url === '/api/employees' && request.params.get('size') === '25');
    first.flush({ items: [{ id: 7, employeeNumber: 'ACM-00007', fullName: 'Alex Lee', email: 'alex@example.com', countryCode: 'US', department: 'Engineering', jobTitle: 'Engineer', jobLevel: 'L2', annualSalary: 85000, currencyCode: 'USD', archived: false }], page: 0, size: 25, totalElements: 1, totalPages: 1 });
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Alex Lee');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('$85,000');

    const search = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>('input[aria-label="Search employees"]')!;
    search.value = 'Priya';
    search.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    const filtered = http.expectOne(request => request.url === '/api/employees' && request.params.get('query') === 'Priya');
    filtered.flush({ items: [], page: 0, size: 25, totalElements: 0, totalPages: 0 });
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('No employees found');
    http.verify();
  });
});
