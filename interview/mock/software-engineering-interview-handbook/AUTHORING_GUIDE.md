# Authoring Guide

This file defines the consistency contract for every chapter in the handbook.

## Writing Contract

- Teach from first principles in clear, professional English.
- Cover beginner, intermediate, and advanced understanding.
- Explain why a design exists before explaining its mechanics.
- Connect theory to production engineering and interview follow-ups.
- Prefer Java for implementations unless another representation is clearer.
- Use portable ASCII diagrams; do not depend on external images.
- Put every interview answer immediately below its question.
- Never leave a question unanswered.
- Explain trade-offs rather than presenting one design as universally best.

## Required Chapter Structure

Every topic chapter uses these headings in this order:

```text
# Topic

## What is it?
## Why do we need it?
## What problem does it solve?
## History (only if useful)
## How does it work?
## Internal Working
## Step-by-Step Example
## Visualization
## Real-World Usage
## Advantages
## Disadvantages
## Trade-offs
## Alternatives
## Performance Analysis
## Complexity Analysis (if applicable)
## Implementation
## Common Mistakes
## Common Interview Questions
## Follow-up Interview Questions
## How the Interviewer Thinks
## Production Engineering Perspective
## Common Misconceptions
## Revision Notes
## Summary
```

If a section is not naturally applicable, state that briefly and explain the
closest relevant engineering consideration rather than omitting the heading.

## Interview Question Format

```text
Q: Why does this mechanism exist?

Answer:
It exists because ...
```

## Chapter Quality Checklist

- Defines all important terms before using them.
- Includes at least one concrete example.
- Includes an ASCII visualization when it improves understanding.
- Includes practical production usage.
- Includes advantages, disadvantages, alternatives, and trade-offs.
- Includes performance or complexity analysis where meaningful.
- Includes common mistakes and misconceptions.
- Includes answered common and follow-up interview questions.
- Ends with concise revision notes and a summary.
