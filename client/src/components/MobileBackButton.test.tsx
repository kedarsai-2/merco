import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import MobileBackButton from "./MobileBackButton";

describe("MobileBackButton", () => {
  it("renders with aria-label for accessibility", () => {
    render(<MobileBackButton onClick={() => {}} />);
    expect(screen.getByRole("button", { name: /go back/i })).toBeInTheDocument();
  });

  it("calls onClick when clicked", () => {
    const onClick = vi.fn();
    render(<MobileBackButton onClick={onClick} />);
    fireEvent.click(screen.getByRole("button", { name: /go back/i }));
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it("applies custom className", () => {
    render(<MobileBackButton onClick={() => {}} className="my-custom-class" />);
    const btn = screen.getByRole("button", { name: /go back/i });
    expect(btn).toHaveClass("my-custom-class");
  });
});
