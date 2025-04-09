import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserRoleService, UserRole } from '../../services/user-role.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="user-management-container">
      <h2>User Management</h2>
      
      <div class="user-list">
        <h3>Users</h3>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Email</th>
              <th>Roles</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let user of users">
              <td>{{ user.id }}</td>
              <td>{{ user.email }}</td>
              <td>
                <span *ngFor="let role of user.roles" class="role-badge">
                  {{ role.name }}
                </span>
              </td>
              <td>
                <button (click)="editUserRoles(user)">Edit Roles</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="role-assignment" *ngIf="selectedUser">
        <h3>Assign Roles to {{ selectedUser.email }}</h3>
        <div class="role-list">
          <div *ngFor="let role of availableRoles" class="role-item">
            <input 
              type="checkbox" 
              [id]="'role-' + role.id"
              [checked]="isRoleAssigned(role)"
              (change)="toggleRole(role)"
            >
            <label [for]="'role-' + role.id">{{ role.name }}</label>
          </div>
        </div>
        <div class="actions">
          <button (click)="saveRoleChanges()">Save Changes</button>
          <button (click)="cancelRoleEdit()">Cancel</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .user-management-container {
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
    
    .role-badge {
      display: inline-block;
      padding: 0.25rem 0.5rem;
      background-color: #e0e0e0;
      border-radius: 4px;
      margin-right: 0.5rem;
      font-size: 0.875rem;
    }
    
    .role-assignment {
      margin-top: 2rem;
      padding: 1rem;
      background-color: #f9f9f9;
      border-radius: 4px;
    }
    
    .role-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 1rem;
      margin: 1rem 0;
    }
    
    .role-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    
    .actions {
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
  `]
})
export class UserManagementComponent implements OnInit {
  users: any[] = [];
  availableRoles: UserRole[] = [];
  selectedUser: any = null;
  selectedRoles: Set<number> = new Set();

  constructor(
    private userRoleService: UserRoleService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // In a real app, you would fetch users from your backend
    // For now, we'll just use the current user as an example
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.users = [currentUser];
    }
    
    // Fetch available roles
    this.userRoleService.getCurrentUserRoles().subscribe(roles => {
      this.availableRoles = roles;
    });
  }

  editUserRoles(user: any) {
    this.selectedUser = user;
    this.selectedRoles.clear();
    
    // Add user's current roles to the selected set
    if (user.roles) {
      user.roles.forEach((role: UserRole) => {
        this.selectedRoles.add(role.id);
      });
    }
  }

  isRoleAssigned(role: UserRole): boolean {
    return this.selectedRoles.has(role.id);
  }

  toggleRole(role: UserRole) {
    if (this.selectedRoles.has(role.id)) {
      this.selectedRoles.delete(role.id);
    } else {
      this.selectedRoles.add(role.id);
    }
  }

  saveRoleChanges() {
    if (!this.selectedUser) return;
    
    // In a real app, you would update the user's roles in your backend
    // For now, we'll just update the local state
    const updatedRoles = this.availableRoles.filter(role => 
      this.selectedRoles.has(role.id)
    );
    
    // Update the user's roles in the users array
    const userIndex = this.users.findIndex(u => u.id === this.selectedUser.id);
    if (userIndex !== -1) {
      this.users[userIndex] = {
        ...this.users[userIndex],
        roles: updatedRoles
      };
    }
    
    this.selectedUser = null;
  }

  cancelRoleEdit() {
    this.selectedUser = null;
  }
} 