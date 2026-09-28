import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { EmployeeApi, EmployeeInput } from './employee-api';

@Component({
  selector: 'app-employee-form',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule],
  templateUrl: './employee-form.html',
  styleUrl: './employee-form.scss',
})
export class EmployeeForm implements OnInit {
  private readonly api = inject(EmployeeApi);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder).nonNullable;
  id = Number(this.route.snapshot.paramMap.get('id')) || null;
  readonly busy = signal(false);
  readonly message = signal('');
  readonly error = signal('');
  readonly archived = signal(false);
  readonly form = this.fb.group({
    employeeNumber: ['', Validators.required],
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    countryCode: ['', [Validators.required, Validators.pattern('[A-Z]{2}')]],
    department: ['', Validators.required],
    jobTitle: ['', Validators.required],
    jobLevel: ['', [Validators.required, Validators.pattern('L[1-5]')]],
    annualSalary: ['', [Validators.required, Validators.min(0.01)]],
    currencyCode: ['USD', Validators.required],
  });

  ngOnInit(): void {
    if (!this.id) return;
    this.busy.set(true);
    this.api.get(this.id).subscribe({
      next: employee => {
        this.form.patchValue({ ...employee, annualSalary: String(employee.annualSalary) });
        this.archived.set(employee.archived);
        this.busy.set(false);
      },
      error: () => { this.error.set('Unable to load employee.'); this.busy.set(false); },
    });
  }

  save(): void {
    this.message.set('');
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Please correct the highlighted fields.');
      return;
    }
    const values = this.form.getRawValue();
    const input: EmployeeInput = { ...values, annualSalary: Number(values.annualSalary) };
    this.busy.set(true);
    const request = this.id ? this.api.update(this.id, input) : this.api.create(input);
    request.subscribe({
      next: employee => {
        this.id = employee.id;
        this.busy.set(false);
        this.message.set('Employee saved');
      },
      error: (response: HttpErrorResponse) => {
        this.busy.set(false);
        this.error.set(response.error?.message ?? 'Unable to save employee. Please try again.');
      },
    });
  }

  archive(): void {
    if (!this.id || this.archived()) return;
    this.busy.set(true);
    this.api.archive(this.id).subscribe({
      next: () => { this.archived.set(true); this.busy.set(false); this.message.set('Employee archived'); },
      error: () => { this.busy.set(false); this.error.set('Unable to archive employee.'); },
    });
  }
}
