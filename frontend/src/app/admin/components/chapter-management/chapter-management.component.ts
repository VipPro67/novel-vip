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
import { NovelService } from "src/app/services/novel.service";

@Component({
  selector: "app-chapter-management",
  templateUrl: "./chapter-management.component.html",
  styleUrls: ["./chapter-management.component.scss"],
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
  ],
})
export class ChapterManagementComponent implements OnInit {
  displayedColumns: string[] = [
    "id",
    "novelTitle",
    "chapterNumber",
    "title",
    "status",
    "createdAt",
    "actions",
  ];
  dataSource: MatTableDataSource<any>;
  chapterForm: FormGroup;
  isEditing = false;
  novels: any[] = []; // Will be populated from API

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private fb: FormBuilder, private dialog: MatDialog, private novelService: NovelService) {
    this.dataSource = new MatTableDataSource<any>([]);
    this.chapterForm = this.fb.group({
      id: [""],
      novelId: ["", Validators.required],
      chapterNumber: ["", [Validators.required, Validators.min(1)]],
      title: ["", Validators.required],
      content: ["", Validators.required],
      status: ["draft", Validators.required],
    });
    this.novelService = novelService;
  }

  ngOnInit(): void {
    this.loadNovels();
    this.loadChapters();
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
    
  }

  loadChapters() {
    // TODO: Implement API call to get chapters
    const mockChapters = [
      {
        id: 1,
        novelId: 1,
        novelTitle: "The Great Adventure",
        chapterNumber: 1,
        title: "The Beginning",
        status: "published",
        createdAt: new Date(),
      },
      {
        id: 2,
        novelId: 1,
        novelTitle: "The Great Adventure",
        chapterNumber: 2,
        title: "The Journey",
        status: "draft",
        createdAt: new Date(),
      },
    ];
    this.dataSource.data = mockChapters;
  }

  onSubmit() {
    if (this.chapterForm.valid) {
      // TODO: Implement API call to create/update chapter
      console.log(this.chapterForm.value);
      this.resetForm();
    }
  }

  editChapter(chapter: any) {
    this.isEditing = true;
    this.chapterForm.patchValue(chapter);
  }

  deleteChapter(chapter: any) {
    if (confirm("Are you sure you want to delete this chapter?")) {
      // TODO: Implement API call to delete chapter
      console.log("Delete chapter:", chapter);
    }
  }

  resetForm() {
    this.isEditing = false;
    this.chapterForm.reset({ status: "draft" });
  }
}
