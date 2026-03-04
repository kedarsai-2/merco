import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import MercotraceLogo, { MercotraceIcon } from "./MercotraceLogo";

describe("MercotraceLogo", () => {
  it("renders with text by default", () => {
    render(<MercotraceLogo />);
    expect(screen.getByText("Mercotrace")).toBeInTheDocument();
  });

  it("hides text when showText is false", () => {
    render(<MercotraceLogo showText={false} />);
    expect(screen.queryByText("Mercotrace")).not.toBeInTheDocument();
  });

  it("renders with different sizes", () => {
    const { rerender } = render(<MercotraceLogo size="sm" showText={false} />);
    expect(document.querySelector("svg")).toBeInTheDocument();

    rerender(<MercotraceLogo size="lg" showText={false} />);
    expect(document.querySelector("svg")).toBeInTheDocument();
  });

  it("applies custom className", () => {
    const { container } = render(<MercotraceLogo className="logo-class" showText={false} />);
    const wrapper = container.firstChild as HTMLElement;
    expect(wrapper).toHaveClass("logo-class");
  });
});

describe("MercotraceIcon", () => {
  it("renders SVG with default size", () => {
    render(<MercotraceIcon />);
    const svg = document.querySelector("svg");
    expect(svg).toBeInTheDocument();
    expect(svg).toHaveAttribute("width", "48");
    expect(svg).toHaveAttribute("height", "48");
  });

  it("renders with custom size", () => {
    render(<MercotraceIcon size={64} />);
    const svg = document.querySelector("svg");
    expect(svg).toHaveAttribute("width", "64");
    expect(svg).toHaveAttribute("height", "64");
  });

  it("renders with custom color", () => {
    render(<MercotraceIcon color="#ff0000" />);
    const path = document.querySelector("svg path");
    expect(path).toHaveAttribute("fill", "#ff0000");
  });
});
