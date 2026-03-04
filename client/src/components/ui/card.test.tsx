import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from "./card";

describe("Card", () => {
  it("renders Card with children", () => {
    render(<Card>Card content</Card>);
    expect(screen.getByText("Card content")).toBeInTheDocument();
  });

  it("renders full card structure", () => {
    render(
      <Card>
        <CardHeader>
          <CardTitle>Card Title</CardTitle>
          <CardDescription>Card description text</CardDescription>
        </CardHeader>
        <CardContent>
          <p>Main content</p>
        </CardContent>
        <CardFooter>Footer content</CardFooter>
      </Card>
    );
    expect(screen.getByText("Card Title")).toBeInTheDocument();
    expect(screen.getByText("Card description text")).toBeInTheDocument();
    expect(screen.getByText("Main content")).toBeInTheDocument();
    expect(screen.getByText("Footer content")).toBeInTheDocument();
  });

  it("CardTitle renders as heading", () => {
    render(
      <Card>
        <CardHeader>
          <CardTitle>Heading</CardTitle>
        </CardHeader>
      </Card>
    );
    const heading = screen.getByRole("heading", { name: /heading/i });
    expect(heading).toBeInTheDocument();
    expect(heading.tagName).toBe("H3");
  });

  it("applies custom className to Card", () => {
    const { container } = render(<Card className="my-card">Content</Card>);
    const card = container.firstChild as HTMLElement;
    expect(card).toHaveClass("my-card");
  });
});
