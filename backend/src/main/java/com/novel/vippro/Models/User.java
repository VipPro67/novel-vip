package com.novel.vippro.Models;

import java.time.Instant;
import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.novel.vippro.Models.base.BaseEntity;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
}, indexes = {
		@Index(name = "idx_username", columnList = "username"),
		@Index(name = "idx_email", columnList = "email"),
		@Index(name = "idx_email_verification_token", columnList = "email_verification_token")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

	@NotBlank
	@Size(max = 20)
	private String username;

	@NotBlank
	@Size(max = 50)
	@Email
	private String email;

	@NotBlank
	@Size(max = 120)
	private String password;

	@Size(max = 120)
	private String fullName;

	@Size(max = 255)
	private String avatar;

	@Column(name = "email_verified")
	private Boolean emailVerified = Boolean.TRUE;

	@Column(name = "email_verification_token", length = 128)
	private String emailVerificationToken;

	@Column(name = "email_verification_sent_at")
	private Instant emailVerificationSentAt;

	@Column(name = "email_verification_expires_at")
	private Instant emailVerificationExpiresAt;

	@Column(name = "email_verified_at")
	private Instant emailVerifiedAt;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private List<Role> roles;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Novel> ownedNovels;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CorrectionRequest> corrections;

	public User(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.emailVerified = Boolean.TRUE;
	}

}
