import { Component, OnInit } from "@angular/core";
import { NgClass, CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from "@angular/forms";
import { Router } from "@angular/router";
import { SupabaseService } from "../../services/supabase.service";

@Component({
  selector: "app-register",
  templateUrl: "./register.component.html",
  styleUrls: ["./register.component.scss"],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
})
export class RegisterComponent implements OnInit {
  registerForm: FormGroup;
  loading = false;
  submitted = false;
  error = "";
  success = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private supabaseService: SupabaseService
  ) {
    this.registerForm = this.formBuilder.group(
      {
        email: ["", [Validators.required, Validators.email]],
        password: ["", [Validators.required, Validators.minLength(6)]],
        confirmPassword: ["", Validators.required],
      },
      { validator: this.passwordMatchValidator }
    );
  }

  ngOnInit() {
    // If user is already logged in, redirect to home
    this.supabaseService.currentUser$.subscribe((user) => {
      if (user) {
        this.router.navigate(["/"]);
      }
    });
  }

  // Custom validator to check if passwords match
  passwordMatchValidator(g: FormGroup) {
    return g.get("password")?.value === g.get("confirmPassword")?.value
      ? null
      : { mismatch: true };
  }

  // Convenience getter for easy access to form fields
  get f() {
    return this.registerForm.controls;
  }

  onSubmit() {
    this.submitted = true;

    // Stop here if form is invalid
    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = "";

    // Sign up with Supabase
    this.supabaseService
      .signUp(this.f["email"].value, this.f["password"].value)
      .then(({ user, error }) => {
        if (error) {
          this.error = error.message;
          this.loading = false;
          return;
        }

        if (user) {
          this.success = true;
          this.loading = false;

          // Redirect to login after 3 seconds
          setTimeout(() => {
            this.router.navigate(["/login"]);
          }, 3000);
        }
      })
      .catch((err) => {
        this.error = "An error occurred during registration";
        this.loading = false;
      });
  }
}
