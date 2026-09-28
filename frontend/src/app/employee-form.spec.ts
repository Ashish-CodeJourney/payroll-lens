import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { EmployeeForm } from './employee-form';

describe('EmployeeForm', () => {
  it('loads a record and saves a changed annual salary', async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeForm],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '7' }) } } }],
    }).compileComponents();
    const fixture = TestBed.createComponent(EmployeeForm);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne('/api/employees/7').flush({ id: 7, employeeNumber: 'ACM-00007', fullName: 'Alex Lee',
      email: 'alex@example.com', countryCode: 'US', department: 'Engineering', jobTitle: 'Engineer',
      jobLevel: 'L2', annualSalary: 85000, currencyCode: 'USD', archived: false });
    fixture.detectChanges();
    const salary = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>('input[aria-label="Annual salary"]')!;
    salary.value = '110000';
    salary.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    const request = http.expectOne('/api/employees/7');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body.annualSalary).toBe(110000);
    request.flush({ ...request.request.body, id: 7, archived: false });
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Employee saved');
    http.verify();
  });

  it('archives an existing record and disables salary editing', async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeForm],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '9' }) } } }],
    }).compileComponents();
    const fixture = TestBed.createComponent(EmployeeForm);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    const employee = { id: 9, employeeNumber: 'ACM-00009', fullName: 'Sam Lee', email: 'sam@example.com',
      countryCode: 'US', department: 'People', jobTitle: 'HR Partner', jobLevel: 'L2', annualSalary: 90000,
      currencyCode: 'USD', archived: false };
    http.expectOne('/api/employees/9').flush(employee);
    fixture.detectChanges();
    const archive = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button'))
      .find(button => button.textContent?.includes('Archive employee'))!;
    archive.click();
    const request = http.expectOne('/api/employees/9/archive');
    expect(request.request.method).toBe('PATCH');
    request.flush({ ...employee, archived: true });
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Employee archived');
    expect((fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button[type="submit"]')).toBeNull();
    const fields = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll<HTMLInputElement | HTMLSelectElement>('input, select'));
    expect(fields.length).toBeGreaterThan(0);
    expect(fields.every(field => field.disabled)).toBe(true);
    http.verify();
  });

  it('shows an already archived employee without editable fields', async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeForm],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '9' }) } } }],
    }).compileComponents();
    const fixture = TestBed.createComponent(EmployeeForm);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne('/api/employees/9').flush({ id: 9, employeeNumber: 'ACM-00009', fullName: 'Sam Lee',
      email: 'sam@example.com', countryCode: 'US', department: 'People', jobTitle: 'HR Partner',
      jobLevel: 'L2', annualSalary: 90000, currencyCode: 'USD', archived: true });
    fixture.detectChanges();
    const root = fixture.nativeElement as HTMLElement;
    const fields = Array.from(root.querySelectorAll<HTMLInputElement | HTMLSelectElement>('input, select'));
    expect(fields.length).toBeGreaterThan(0);
    expect(fields.every(field => field.disabled)).toBe(true);
    expect(root.querySelector<HTMLInputElement>('input[aria-label="Annual salary"]')?.value).toBe('90000');
    expect(root.textContent).toContain('Archived employee');
    expect(root.textContent).toContain('Archived records are read-only.');
    expect(root.querySelector<HTMLButtonElement>('button[type="submit"]')).toBeNull();
    http.verify();
  });

  it('creates a valid employee from the form', async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeForm],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([
        { path: 'employees/:id', component: EmployeeForm },
      ]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({}) } } }],
    }).compileComponents();
    const fixture = TestBed.createComponent(EmployeeForm);
    const http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    const values = { 'Employee number': 'ACM-99999', 'Full name': 'Priya Shah',
      'Work email': 'priya@example.com', 'Country code': 'IN', Department: 'Engineering',
      'Job title': 'Engineer', 'Job level': 'L3', 'Annual salary': '1200000' };
    for (const [label, value] of Object.entries(values)) {
      const input = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>(`input[aria-label="${label}"]`)!;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    fixture.detectChanges();
    (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('button[type="submit"]')!.click();
    const request = http.expectOne('/api/employees');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.annualSalary).toBe(1200000);
    request.flush({ ...request.request.body, id: 99, archived: false });
    await fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/employees/99');
    http.verify();
  });
});
