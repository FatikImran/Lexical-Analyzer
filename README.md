# Fate Language Scanner (Assignment 1)

## Language Overview
- Language name: Fate
- File extension: .fate

## Keywords
- start: program start
- finish: program end
- loop: loop construct
- condition: conditional construct
- declare: variable declaration
- output: output statement
- input: input statement
- function: function declaration
- return: return statement
- break: loop control
- continue: loop control
- else: alternative branch

## Identifiers
- Pattern: start with uppercase letter (A-Z), then lowercase letters, digits, or underscore
- Max length: 31 characters
- Valid: Count, Total_1, X
- Invalid: count, MyVar (uppercase in body), 2Count

## Literals
- Integer: [+-]?[0-9]+
  - Examples: 42, +100, -567, 0
- Floating-point: [+-]?[0-9]+\.[0-9]{1,6}([eE][+-]?[0-9]+)?
  - Examples: 3.14, -0.123456, 1.5e10, 2.0E-3
- String: "..." with escapes (\", \\, \n, \t, \r)
- Character: 'c' or escape (\', \\, \n, \t, \r)
- Boolean: true | false

## Operators (by category)
- Arithmetic: +, -, *, /, %, **
- Relational: ==, !=, <=, >=, <, >
- Logical: &&, ||, !
- Assignment: =, +=, -=, *=, /=
- Inc/Dec: ++, --

## Punctuators
- ( ) { } [ ] , ; :

## Comments
- Single-line: ## comment
- Multi-line: #* comment *#

## Sample Programs

### Sample 1: Declarations and Output
```
start
    declare Count = 10
    declare Rate = 3.14
    output "Count is:"
    output Count
finish
```

### Sample 2: Condition and Loop
```
start
    declare Count = 3
    loop (Count > 0) {
        output Count
        Count -= 1
    }
    condition (Count == 0) {
        output "Done"
    } else {
        output "Not done"
    }
finish
```

### Sample 3: Function with Return
```
function Sum
    declare A = 2
    declare B = 3
    return A + B
finish
```

## Build and Run

### Manual Scanner
```
cd 23i0655-23i0524-C
javac src/*.java
java -cp src Main tests/test1.fate
```

### JFlex Scanner
```
cd 23i0655-23i0524-C/src
java -jar c:/jflex-1.9.1/jflex-1.9.1/lib/jflex-full-1.9.1.jar Scanner.flex
cd ..
javac src/*.java
java -cp src JFlexRunner tests/test1.fate
```

### Compare Outputs
```
cd 23i0655-23i0524-C
java -cp src CompareScanners tests/test1.fate
```

## Team Members
- Muhammad Fatik 23i0655
- Muhammad Kaleem 23i0524
