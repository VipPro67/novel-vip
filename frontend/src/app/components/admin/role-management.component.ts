import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserRoleService, UserRole } from '../../services/user-role.service';

@Component({
  selector: 'app-role-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="role-management-container">
      <h2>Role Management</h2>
      
      <div class="role-list">
        <h3>Available Roles</h3>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Description</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let role of roles">
              <td>{{ role.id }}</td>
              <td>{{ role.name }}</td>
              <td>{{ role.description || 'No description' }}</td>
              <td>
                <button (click)="editRole(role)">Edit</button>
                <button (click)="deleteRole(role)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="role-form">
        <h3>{{ isEditing ? 'Edit Role' : 'Create New Role' }}</h3>
        <form (ngSubmit)="saveRole()">
          <div class="form-group">
            <label for="roleName">Role Name</label>
            <input 
              type="text" 
              id="roleName" 
              [(ngModel)]="currentRole.name" 
              name="roleName" 
              required
            >
          </div>
          
          <div class="form-group">
            <label for="roleDescription">Description</label>
            <textarea 
              id="roleDescription" 
              [(ngModel)]="currentRole.description" 
              name="roleDescription"
            ></textarea>
          </div>
          
          <div class="form-actions">
            <button type="submit">Save</button>
            <button type="button" (click)="cancelEdit()">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .role-management-container {
      padding: 1rem;
    }
    
    table {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 2rem;
    }
    
    th, td {
      padding: 0.75rem;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }
    
    th {
      background-color: #f5f5f5;
    }
    
    .role-form {
      margin-top: 2rem;
      padding: 1rem;
      background-color: #f9f9f9;
      border-radius: 4px;
    }
    
    .form-group {
      margin-bottom: 1rem;
    }
    
    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: bold;
    }
    
    input, textarea {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid #ddd;
      border-radius: 4px;
    }
    
    textarea {
      min-height: 100px;
    }
    
    .form-actions {
      display: flex;
      gap: 1rem;
      margin-top: 1rem;
    }
    
    button {
      padding: 0.5rem 1rem;
      background-color: #4a90e2;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    
    button:hover {
      background-color: #3a7bc8;
    }
    
    button[type="button"] {
      background-color: #6c757d;
    }
    
    button[type="button"]:hover {
      background-color: #5a6268;
    }
  `]
})
export class RoleManagementComponent implements OnInit {
  roles: UserRole[] = [];
  currentRole: UserRole = { id: 0, name: '', description: '' };
  isEditing = false;

  constructor(private userRoleService: UserRoleService) {}

  ngOnInit() {
    // In a real app, you would fetch roles from your backend
    // For now, we'll just use some example roles
    this.roles = [
      { id: 1, name: 'ADMIN', description: 'Administrator with full access' },
      { id: 2, name: 'USER', description: 'Regular user with limited access' },
      { id: 3, name: 'EDITOR', description: 'Content editor with publishing rights' }
    ];
  }

  editRole(role: UserRole) {
    this.currentRole = { ...role };
    this.isEditing = true;
  }

  deleteRole(role: UserRole) {
    if (confirm(`Are you sure you want to delete the role "${role.name}"?`)) {
      // In a real app, you would delete the role from your backend
      // For now, we'll just remove it from the local state
      this.roles = this.roles.filter(r => r.id !== role.id);
    }
  }

  saveRole() {
    if (this.isEditing) {
      // Update existing role
      const index = this.roles.findIndex(r => r.id === this.currentRole.id);
      if (index !== -1) {
        this.roles[index] = { ...this.currentRole };
      }
    } else {
      // Create new role
      const newRole = { 
        ...this.currentRole, 
        id: Math.max(...this.roles.map(r => r.id)) + 1 
      };
      this.roles.push(newRole);
    }
    
    this.resetForm();
  }

  cancelEdit() {
    this.resetForm();
  }

  private resetForm() {
    this.currentRole = { id: 0, name: '', description: '' };
    this.isEditing = false;
  }
} 