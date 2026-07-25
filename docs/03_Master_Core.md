# Master Core

## Overview

The Master Core is the identity of Aadhini OS.

It initializes the system, maintains the operating state and coordinates all major components.

## Responsibilities

- Initialize the operating system
- Maintain system identity
- Coordinate core modules
- Monitor system state
- Maintain lifecycle

## Connected Modules

- Decision Engine
- Memory Engine
- Guardian Engine
- Task Manager

## Engineering Rules

- Never execute business logic directly.
- Delegate execution through the Decision Engine.
- Remain active throughout the system lifecycle.
- Maintain system integrity.