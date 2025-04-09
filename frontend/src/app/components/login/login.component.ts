import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from "@angular/forms";
import { Router, ActivatedRoute } from "@angular/router";
import { SupabaseService } from "../../services/supabase.service";

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  submitted = false;
  error = "";
  returnUrl: string = "/";

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private supabaseService: SupabaseService
  ) {
    this.loginForm = this.formBuilder.group({
      email: ["", [Validators.required, Validators.email]],
      password: ["", Validators.required],
    });
  }

  ngOnInit() {
    // Get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams["returnUrl"] || "/";
    console.log("Current user:", this.supabaseService.getAccessToken());
  }

  // Convenience getter for easy access to form fields
  get f() {
    return this.loginForm.controls;
  }

  onSubmit() {
    this.submitted = true;

    // Stop here if form is invalid
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = "";

    // Sign in with Supabase
    this.supabaseService
      .signIn(this.f["email"].value, this.f["password"].value)
      .then(({ user, error }) => {
        if (error) {
          this.error = error.message;
          this.loading = false;
          return;
        }

        if (user) {
          // Verify token with backend
          this.supabaseService
            .verifyToken()
            .then((result) => {
              if (result.valid) {
                // Navigate to return url
                this.router.navigate([this.returnUrl]);
              } else {
                this.error = "Authentication failed";
                this.loading = false;
              }
            })
            .catch((err) => {
              this.error = "Failed to verify token";
              this.loading = false;
            });
        }
      })
      .catch((err) => {
        this.error = "An error occurred during login";
        this.loading = false;
      });
  }

  signInWithGoogle() {
    this.loading = true;
    this.error = "";

    this.supabaseService.signInWithOAuth("google").then(({ error }) => {
      if (error) {
        this.error = error.message;
        this.loading = false;
      }
    });
  }
}
