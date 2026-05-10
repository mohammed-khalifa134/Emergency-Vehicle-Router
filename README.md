# Emergency Vehicle Router System

A Java 21 object-oriented emergency routing system that demonstrates how to dispatch emergency vehicles, calculate routes, react to traffic updates, and compare routing algorithms.

The project is built for learning clean architecture, SOLID principles, and three common design patterns:

- Factory Pattern
- Strategy Pattern
- Observer Pattern

## Features

- Creates emergency vehicles dynamically.
- Receives emergency reports.
- Selects a suitable available vehicle.
- Calculates routes using interchangeable strategies.
- Models a road network as a graph.
- Runs Dijkstra's Algorithm for live shortest-path routing.
- Runs Hub Label routing for faster preprocessed queries.
- Updates road traffic and closures in real time.
- Recalculates the active route automatically after traffic changes.

## Requirements

- Java JDK 21
- Maven is optional, but recommended

The project uses `javac --release 21` compatible source code.

## Project Structure

```text
src/
|-- main/
|   `-- java/
|       `-- com/
|           `-- emergencyrouter/
|               |-- enums/
|               |-- factory/
|               |-- interfaces/
|               |-- main/
|               |-- model/
|               |   `-- vehicles/
|               |-- observer/
|               |-- service/
|               `-- strategy/
`-- test/
    `-- java/
        `-- com/
            `-- emergencyrouter/
```

## Package Responsibilities

- `enums`: shared enums such as `VehicleStatus`.
- `interfaces`: small contracts such as `Location`.
- `model`: core domain objects such as reports, routes, graph nodes, edges, traffic data, and hub labels.
- `model.vehicles`: vehicle hierarchy: `Vehicle`, `Ambulance`, `FireTruck`, and `PoliceCar`.
- `factory`: vehicle creation logic.
- `service`: business services for dispatching, routing, traffic updates, and Hub Label preprocessing.
- `strategy`: route calculation strategies.
- `observer`: observer contracts used for traffic notifications.
- `main`: console simulation entry point.

## Main Workflow

The console demo in `com.emergencyrouter.main.Main` shows the full system:

1. Build a sample road graph.
2. Create vehicles using `VehicleFactory`.
3. Create an emergency report.
4. Dispatch the correct vehicle.
5. Calculate a route using Dijkstra.
6. Preprocess and run Hub Label routing.
7. Print precomputed Hub Label routes.
8. Apply a traffic update.
9. Notify observers.
10. Recalculate the active route automatically.

## Running With Maven

If Maven is installed and available on your PATH:

```powershell
mvn test
mvn exec:java -Dexec.mainClass="com.emergencyrouter.main.Main"
```

This `pom.xml` currently configures Java 21 compilation and JUnit 4 tests. If the `exec:java` command is not available in your Maven setup, use the direct `javac` commands below.

## Running With javac

From the project root:

```powershell
$files = Get-ChildItem -Path src\main\java -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac --release 21 -d target\classes $files
java -cp target\classes com.emergencyrouter.main.Main
```

## Running Tests Without Maven

If Maven is not installed, you can compile and run the JUnit tests manually after JUnit exists in your local Maven cache:

```powershell
$junit = "$env:USERPROFILE\.m2\repository\junit\junit\4.13.2\junit-4.13.2.jar"
$hamcrest = "$env:USERPROFILE\.m2\repository\org\hamcrest\hamcrest-core\1.3\hamcrest-core-1.3.jar"
$mainFiles = Get-ChildItem -Path src\main\java -Recurse -Filter *.java | ForEach-Object { $_.FullName }
$testFiles = Get-ChildItem -Path src\test\java -Recurse -Filter *.java | ForEach-Object { $_.FullName }

javac --release 21 -d target\classes $mainFiles
javac --release 21 -cp "target\classes;$junit;$hamcrest" -d target\test-classes $testFiles
java -cp "target\classes;target\test-classes;$junit;$hamcrest" org.junit.runner.JUnitCore `
  com.emergencyrouter.factory.VehicleFactoryTest `
  com.emergencyrouter.model.GraphTest `
  com.emergencyrouter.service.DispatchServiceTest `
  com.emergencyrouter.service.RoutingServiceTest `
  com.emergencyrouter.service.TrafficServiceTest `
  com.emergencyrouter.strategy.BasicRouteStrategyTest `
  com.emergencyrouter.strategy.DijkstraRouteStrategyTest `
  com.emergencyrouter.strategy.HubLabelRouteStrategyTest
```

## Design Patterns

### Factory Pattern

`VehicleFactory` creates vehicles from a type string and hides constructor details from the rest of the system.

The factory uses a registry, so adding a new vehicle type does not require rewriting every switch or `instanceof` check.

### Strategy Pattern

`RoutingService` depends on the `RouteStrategy` interface.

Available strategies:

- `FastestRouteStrategy`
- `ShortestDistanceStrategy`
- `DijkstraRouteStrategy`
- `HubLabelRouteStrategy`

This allows the system to switch algorithms at runtime:

```java
routingService.setStrategy(new DijkstraRouteStrategy(graph));
```

### Observer Pattern

`TrafficService` is the subject.

`RoutingService` is an observer.

When traffic changes, `TrafficService` notifies `RoutingService`, and the route is recalculated automatically.

## Routing Algorithms

### Dijkstra's Algorithm

`DijkstraRouteStrategy` calculates the shortest route on the current graph.

It uses:

- a `PriorityQueue` to always expand the cheapest known node next
- `distance + trafficWeight` as the effective road cost
- closed-road checks so blocked roads are skipped
- path reconstruction after the destination is reached

Complexity:

- Time: `O((V + E) log V)`
- Space: `O(V + E)`

Where `V` is the number of nodes and `E` is the number of edges.

### Hub Label Routing

`HubLabelPreprocessor` calculates reusable shortest-path data once.

`HubLabelRouteStrategy` answers route queries by comparing common hubs between source and destination labels.

This makes emergency queries faster after preprocessing, but it uses more memory and must refresh labels when traffic changes.

Complexity:

- Preprocessing: expensive because shortest-path information is generated ahead of time
- Query: fast because it compares precomputed labels
- Tradeoff: more memory for lower query latency

## SOLID Principles

- Single Responsibility Principle: each class has one clear job.
- Open/Closed Principle: new vehicles and route strategies can be added with minimal changes.
- Liskov Substitution Principle: any vehicle subclass can be used where `Vehicle` is expected.
- Interface Segregation Principle: small focused interfaces keep classes from depending on methods they do not use.
- Dependency Inversion Principle: services depend on abstractions such as `RouteStrategy`, `Observer`, and `Location`.

## Example Console Output

```text
Emergency Vehicle Router System started.
Building road graph...
Emergency report received: MEDICAL
Available Ambulance selected.
Running Dijkstra shortest path...
Dijkstra route:
Path: Station -> Downtown -> Bridge -> Incident

Preprocessing Hub Labels...
Hub Label precomputed routes:
Station -> Bridge(2.00), Bypass(2.50), Downtown(1.00), Incident(3.00)

Running Hub Label shortest path...
Route comparison:
Dijkstra query time: 3.985 ms
Hub Label query time: 2.431 ms

Traffic update received:
Road Station->Downtown closed
Recalculating route...
Updated emergency route:
Path: Station -> Bypass -> Incident
Alternative path selected.
```

Exact timing values will vary by machine.

## Adding A New Vehicle Type

1. Create a new class in `model.vehicles` that extends `Vehicle`.
2. Implement its emergency response behavior.
3. Register it in `VehicleFactory`.
4. Give the vehicle its supported report type through its constructor.

The dispatch service will use `vehicle.canHandle(report)` instead of hardcoded `instanceof` checks.

## Current Test Coverage

The project includes focused tests for:

- vehicle factory creation
- invalid vehicle types
- dispatch selection
- strategy switching
- graph updates
- Dijkstra pathfinding
- Hub Label preprocessing and routing
- observer notification
- traffic-triggered route recalculation
