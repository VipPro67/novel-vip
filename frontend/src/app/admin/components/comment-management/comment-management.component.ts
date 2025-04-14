import { Component, OnInit, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatTableDataSource } from "@angular/material/table";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from "@angular/forms";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatTableModule } from "@angular/material/table";
import { MatAutocompleteModule } from "@angular/material/autocomplete";

@Component({
  selector: "app-comment-management",
  templateUrl: "./comment-management.component.html",
  styleUrls: ["./comment-management.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatPaginatorModule,
    MatSortModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    MatAutocompleteModule,
  ],
})
export class CommentManagementComponent implements OnInit {
  displayedColumns: string[] = [
    "id",
    "user",
    "novelTitle",
    "chapterNumber",
    "content",
    "status",
    "createdAt",
    "actions",
  ];
  dataSource: MatTableDataSource<any>;
  commentForm: FormGroup;
  isEditing = false;
  novels: any[] = []; // Will be populated from API

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private fb: FormBuilder, private dialog: MatDialog) {
    this.dataSource = new MatTableDataSource<any>([]);
    this.commentForm = this.fb.group({
      id: [""],
      userId: ["", Validators.required],
      novelId: ["", Validators.required],
      chapterId: [""],
      content: ["", Validators.required],
      status: ["pending", Validators.required],
    });
  }

  ngOnInit(): void {
    this.loadNovels();
    this.loadComments();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  loadNovels() {
    // TODO: Implement API call to get novels
    this.novels = [
      { id: 1, title: "The Great Adventure" },
      { id: 2, title: "Mystery of the Night" },
    ];
  }

  loadComments() {
    // TODO: Implement API call to get comments
    const mockComments = [
      {
        id: 1,
        user: "John Doe",
        novelTitle: "The Great Adventure",
        chapterNumber: 1,
        content: "Great chapter!",
        status: "approved",
        createdAt: new Date(),
      },
      {
        id: 2,
        user: "Jane Smith",
        novelTitle: "The Great Adventure",
        chapterNumber: 2,
        content: "Looking forward to the next chapter!",
        status: "pending",
        createdAt: new Date(),
      },
    ];
    this.dataSource.data = mockComments;
  }

  onSubmit() {
    if (this.commentForm.valid) {
      // TODO: Implement API call to create/update comment
      console.log(this.commentForm.value);
      this.resetForm();
    }
  }

  editComment(comment: any) {
    this.isEditing = true;
    this.commentForm.patchValue(comment);
  }

  deleteComment(comment: any) {
    if (confirm("Are you sure you want to delete this comment?")) {
      // TODO: Implement API call to delete comment
      console.log("Delete comment:", comment);
    }
  }

  resetForm() {
    this.isEditing = false;
    this.commentForm.reset({ status: "pending" });
  }
}
