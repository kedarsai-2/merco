import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { Input } from "./input";

describe("Input", () => {
  it("renders with placeholder", () => {
    render(<Input placeholder="Enter text" />);
    expect(screen.getByPlaceholderText("Enter text")).toBeInTheDocument();
  });

  it("accepts and displays value", () => {
    render(<Input value="test value" onChange={() => {}} />);
    const input = screen.getByDisplayValue("test value");
    expect(input).toBeInTheDocument();
  });

  it("calls onChange when user types", () => {
    const onChange = vi.fn();
    render(<Input placeholder="Type here" onChange={onChange} />);
    const input = screen.getByPlaceholderText("Type here");
    fireEvent.change(input, { target: { value: "a" } });
    expect(onChange).toHaveBeenCalled();
  });

  it("is disabled when disabled prop is true", () => {
    render(<Input disabled placeholder="Disabled" />);
    expect(screen.getByPlaceholderText("Disabled")).toBeDisabled();
  });

  it("renders with type password", () => {
    render(<Input type="password" placeholder="Password" />);
    const input = screen.getByPlaceholderText("Password");
    expect(input).toHaveAttribute("type", "password");
  });

  it("applies custom className", () => {
    render(<Input className="custom-input" placeholder="Custom" />);
    const input = screen.getByPlaceholderText("Custom");
    expect(input).toHaveClass("custom-input");
  });
});
