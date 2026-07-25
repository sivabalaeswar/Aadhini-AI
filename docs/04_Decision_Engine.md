# Decision Engine

## Overview

The Decision Engine is the brain of Aadhini OS.

Every request passes through the Decision Engine before execution.

No module may execute actions without Decision Engine approval.

## Responsibilities

- Analyze user intent
- Validate execution rules
- Query Memory Engine
- Evaluate security constraints
- Select execution strategy
- Create execution plan
- Approve or reject actions

## Inputs

- User Intent
- Memory
- Rules
- Context
- Knowledge
- Trust Level

## Outputs

- Approved Action
- Rejected Action
- Request Confirmation
- Alternative Recommendation

## Connected Modules

- Master Core
- Memory Engine
- Rule Engine
- Task Manager
- Guardian Engine

## Engineering Rules

- Every decision must be explainable.
- Every decision must be logged.
- Never bypass security validation.
- Never execute directly.
- Delegate execution to Task Manager.