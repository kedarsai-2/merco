import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen } from "@testing-library/react";
import PortraitLock from "./PortraitLock";

describe("PortraitLock", () => {
  const originalInnerWidth = window.innerWidth;
  const originalInnerHeight = window.innerHeight;

  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
    Object.defineProperty(window, "innerWidth", { value: originalInnerWidth, writable: true });
    Object.defineProperty(window, "innerHeight", { value: originalInnerHeight, writable: true });
  });

  it("returns null when not in landscape (desktop size)", () => {
    Object.defineProperty(window, "innerWidth", { value: 1200, writable: true });
    Object.defineProperty(window, "innerHeight", { value: 800, writable: true });
    const { container } = render(<PortraitLock />);
    expect(container.firstChild).toBeNull();
  });

  it("returns null when height is large (not mobile landscape)", () => {
    Object.defineProperty(window, "innerWidth", { value: 800, writable: true });
    Object.defineProperty(window, "innerHeight", { value: 700, writable: true });
    const { container } = render(<PortraitLock />);
    expect(container.firstChild).toBeNull();
  });

  it("shows overlay when in mobile landscape", () => {
    Object.defineProperty(window, "innerWidth", { value: 800, writable: true });
    Object.defineProperty(window, "innerHeight", { value: 400, writable: true });
    render(<PortraitLock />);
    expect(screen.getByRole("heading", { name: /please rotate your device/i })).toBeInTheDocument();
    expect(screen.getByText(/mercotrace works best in portrait mode/i)).toBeInTheDocument();
  });

  it("adds and removes resize listener", () => {
    const addSpy = vi.spyOn(window, "addEventListener");
    const removeSpy = vi.spyOn(window, "removeEventListener");
    const { unmount } = render(<PortraitLock />);
    expect(addSpy).toHaveBeenCalledWith("resize", expect.any(Function));
    unmount();
    expect(removeSpy).toHaveBeenCalledWith("resize", expect.any(Function));
    addSpy.mockRestore();
    removeSpy.mockRestore();
  });
});
