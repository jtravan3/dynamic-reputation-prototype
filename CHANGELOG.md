All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.2]

### Fixed

- Changed concurrency of executions for growing, shrinking phases and executions

## [2.1.1]

### Fixed

- Fixed a logic error when checking conflicting and abort percentages

## [2.1.0]

### Added

- Begin execution of Use Case 13
- Updated schedulers to reflect growing and shrinking phase of 2PL
- More accurate overall execution time tracking

## [2.0.9]

### Added

- Begin execution of Use Case 12

## [2.0.8]

### Added

- Begin execution of Use Case 11

## [2.0.7]

### Added

- Begin execution of Use Case 10

## [2.0.6]

### Added

- Begin execution of Use Case 9

## [2.0.5]

### Added

- Begin execution of Use Case 8

## [2.0.4]

### Fixed

- Logic error found

## [2.0.3]

### Fixed

- Fixed thresholds to record appropriately

## [2.0.2]

### Changed

- Added use cases 7 and 8
- Added the ability for transction thresholds

## [2.0.1]

### Changed

- Changed random aborts to be more realistic

## [2.0.0]

### Changed

- Added minimum transactions in the system before recalculation occurs
- Added traditional scheduler execution
- Added pbs scheduler execution
- Multi-threaded recalculation

## [1.0.9]

### Changed

- Modified thread pool executor to handle more threads

## [1.0.8]

### Added

- Added use case columns to appropriate tables so that is configurable
- Created view for use case 3 metrics

## [1.0.7]

### Added

- Created view for use case 2 metrics

### Fixed

- Changed when I increment affected transactions

## [1.0.6]

### Added

- Created view for use case 1 metrics
- Created table for tracking use cases

## [1.0.5]

### Added

- Changed the increments on the affected transactions

## [1.0.4]

### Added

- Added the ability to configure and view total transactions affected and total transactions executed

### Fixed

- Fixed a NPE issue with retrieving Transactions and Users

## [1.0.3]

### Added

- App deployments updated

## [1.0.2]

### Added

- Added overall execution ID

## [1.0.1]

### Added

- First live deployment

## [1.0.0]

### Added

- Initial creation
