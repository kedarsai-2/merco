import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";

const mockUseAuth = vi.fn();
vi.mock("@/context/AuthContext", () => ({
  useAuth: () => mockUseAuth(),
}));

describe("ProtectedRoute", () => {
  it("shows loading spinner when not bootstrapped", () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, hasBootstrapped: false });
    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div>Protected content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );
    expect(screen.queryByText("Protected content")).not.toBeInTheDocument();
    expect(document.querySelector(".animate-spin")).toBeInTheDocument();
  });

  it("redirects to login when bootstrapped but not authenticated", () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, hasBootstrapped: true });
    render(
      <MemoryRouter initialEntries={["/dashboard"]}>
        <ProtectedRoute>
          <div>Protected content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );
    expect(screen.queryByText("Protected content")).not.toBeInTheDocument();
    expect(document.querySelector('[class*="animate-spin"]')).not.toBeInTheDocument();
  });

  it("renders children when authenticated", () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, hasBootstrapped: true });
    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div>Protected content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );
    expect(screen.getByText("Protected content")).toBeInTheDocument();
  });
});
