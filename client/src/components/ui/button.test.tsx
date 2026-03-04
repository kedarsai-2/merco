import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { Button } from "./button";

describe("Button", () => {
  it("renders with default variant and size", () => {
    render(<Button>Click me</Button>);
    expect(screen.getByRole("button", { name: /click me/i })).toBeInTheDocument();
  });

  it("calls onClick when clicked", async () => {
    const onClick = vi.fn();
    render(<Button onClick={onClick}>Submit</Button>);
    fireEvent.click(screen.getByRole("button", { name: /submit/i }));
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it("renders destructive variant", () => {
    render(<Button variant="destructive">Delete</Button>);
    const btn = screen.getByRole("button", { name: /delete/i });
    expect(btn).toHaveClass("bg-destructive");
  });

  it("renders outline variant", () => {
    render(<Button variant="outline">Cancel</Button>);
    const btn = screen.getByRole("button", { name: /cancel/i });
    expect(btn).toHaveClass("border");
  });

  it("renders small size", () => {
    render(<Button size="sm">Small</Button>);
    const btn = screen.getByRole("button", { name: /small/i });
    expect(btn).toHaveClass("h-9");
  });

  it("renders large size", () => {
    render(<Button size="lg">Large</Button>);
    const btn = screen.getByRole("button", { name: /large/i });
    expect(btn).toHaveClass("h-11");
  });

  it("is disabled when disabled prop is true", () => {
    render(<Button disabled>Disabled</Button>);
    expect(screen.getByRole("button", { name: /disabled/i })).toBeDisabled();
  });

  it("applies custom className", () => {
    render(<Button className="custom-class">Custom</Button>);
    const btn = screen.getByRole("button", { name: /custom/i });
    expect(btn).toHaveClass("custom-class");
  });
});
