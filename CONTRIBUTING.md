# Contributing to LibreFit

First off, thanks for taking the time to contribute! ❤️

All types of contributions are encouraged and valued. See
the [Table of Contents](#table-of-contents) for different ways to help and details about how this
project handles them. Please make sure to read the relevant section before making your contribution.
It will make it a lot easier for us maintainers and smooth out the experience for all involved. The
community looks forward to your contributions. 🎉

> And if you like the project, but just don't have time to contribute, that's fine. There are other
> easy ways to support the project and show your appreciation, which we would also be very happy
> about:
> - Star the project
> - Tweet or post about it
> - Mention the project at local meetups and tell your friends/colleagues
> - Refer this project in your project's readme

## Table of Contents

- [Code of Conduct](CONTRIBUTING.md#code-of-conduct)
- [I Have a Question](CONTRIBUTING.md#i-have-a-question)
- [I Want To Contribute](CONTRIBUTING.md#i-want-to-contribute)
    - [License](CONTRIBUTING.md#license)
    - [Reporting Bugs](CONTRIBUTING.md#reporting-bugs)
    - [Suggesting Enhancements](CONTRIBUTING.md#suggesting-enhancements)
    - [Your First Code Contribution](CONTRIBUTING.md#your-first-code-contribution)
    - [Translations](CONTRIBUTING.md#translations)

## Code of Conduct

This project and everyone participating in it is governed by
the [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.
Please report unacceptable behavior by filling this [form](https://librefit.org/contact).

## I Have a Question

Before you ask a question, it is best to search for
existing [Discussions](https://github.com/LibreFitOrg/LibreFit/discussions)
and [Issues](https://github.com/LibreFitOrg/LibreFit/issues) that might help you. In case you have
found a suitable issue and still need clarification, you can write your question in this discussion.
It is also advisable to search the internet for answers first.

If you then still feel the need to ask a question and need clarification, we recommend the
following:

- Open an [Discussion](https://github.com/LibreFitOrg/LibreFit/discussions/new).
- Provide as much context as you can about what you're running into.
- Provide project and platform versions, depending on what seems relevant.

We will then take care of the question as soon as possible.

## I Want To Contribute

When contributing to this project, you must agree that you have authored 100% of the content, that
you have the necessary rights to the content and that the content you contribute may be provided
under the project licence.

### License

By contributing to **LibreFit**, you agree that your contributions will be licensed under the **GNU
General Public License v3.0 (GPLv3)**.

### Reporting Bugs

#### Before Submitting a Bug Report

A good bug report shouldn't leave others needing to chase you up for more information. Therefore, we
ask you to investigate carefully, collect information and describe the issue in detail in your
report. Please complete the following steps in advance to help us fix any potential bug as fast as
possible.

- Make sure that you are using the latest version.
- Determine if your bug is really a bug and not an error on your side e.g. using incompatible
  environment components/versions (Make sure that you have read the [README](README.md). If you are
  looking for support, you might want to check [this section](CONTRIBUTING.md#i-have-a-question)).
- To see if other users have experienced (and potentially already solved) the same issue you are
  having, check if there is not already a bug report existing for your bug or error in
  the [bug tracker](https://github.com/LibreFitOrg/LibreFit/issues?q=label%3Abug).
- Also make sure to search the internet to see if users outside of the GitHub community have
  discussed the issue.

#### How Do I Submit a Good Bug Report?

> You must never report security related issues, vulnerabilities or bugs including sensitive
> information to the issue tracker, or elsewhere in public. Instead sensitive bugs must be sent using
> the [GitHub vulnerability report](https://github.com/LibreFitOrg/LibreFit/security/advisories/new).

We use GitHub issues to track bugs and errors. If you run into an issue with the project:

- Open an [Issue](https://github.com/LibreFitOrg/LibreFit/issues/new). (Since we can't be sure at
  this point whether it is a bug or not, we ask you not to talk about a bug yet and not to label the
  issue.)
- Fill the provided template.
- Please provide as much context as possible and describe the *reproduction steps* that someone else
  can follow to recreate the issue on their own. For good bug reports you should isolate the problem
  and create a reduced test case.

Once it's filed:

- The project team will label the issue accordingly.
- A team member will try to reproduce the issue with your provided steps. If there are no
  reproduction steps or no obvious way to reproduce the issue, the team will ask you for those steps
  and mark the issue as `needs-repro`. Bugs with the `needs-repro` tag will not be addressed until
  they are reproduced.
- If the team is able to reproduce the issue, it will be marked `needs-fix`, as well as possibly
  other tags (such as `critical`), and the issue will be left to
  be [implemented by someone](#your-first-code-contribution).

### Suggesting Enhancements

This section guides you through submitting an enhancement suggestion for LibreFit, **including
completely new features and minor improvements to existing functionality**. Following these
guidelines will help maintainers and the community to understand your suggestion and find related
suggestions.

#### Before Submitting an Enhancement

- Make sure that you are using the latest version.
- Read the [README.md](README.md) carefully and find out if the functionality is already covered,
  maybe by an individual configuration.
- Perform a search in [Existing Issues](https://github.com/LibreFitOrg/LibreFit/issues)
  and [Existing Discussions](https://github.com/LibreFitOrg/LibreFit/discussions) to see if the
  enhancement has already been suggested. If it has, add a comment to the existing issue/discussion
  instead of opening a new one.
- Find out whether your idea fits with the scope and aims of the project. It's up to you to make a
  strong case to convince the project's developers of the merits of this feature. Keep in mind that
  we want features that will be useful to the majority of our users and not just a small subset.

#### How Do I Submit a Good Enhancement Suggestion?

Enhancement suggestions are tracked
as [GitHub issues](https://github.com/LibreFitOrg/LibreFit/issues).

Fill the provided template. In particular:

- Use a **clear and descriptive title** for the issue to identify the suggestion.
- Provide a **step-by-step description of the suggested enhancement** in as many details as
  possible.
- **Describe the current behavior** and **explain which behavior you expected to see instead** and
  why. At this point you can also tell which alternatives do not work for you.
- You may want to **include screenshots or screen recordings** which help you demonstrate the steps
  or point out the part which the suggestion is related to.
- **Explain why this enhancement would be useful** to most LibreFit users. You may also want to
  point out the other projects that solved it better and which could serve as inspiration.

### Your First Code Contribution

We strive to keep the codebase clean, readable, and maintainable while following the current
architecture and the *
*[Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)**.

#### Architecture

This app follows a **Clean Architecture** approach with an MVVM presentation layer and is built with
the latest Android toolkit:

- [MVVM](https://developer.android.com/topic/architecture/recommendations) – [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) + [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
  for UI state.
- [Jetpack Compose](https://developer.android.com/compose) for UI.
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for dependency
  injection.
- [Room](https://developer.android.com/training/data-storage/room) for storage of workouts,
  exercises, sets and dataset.
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for user
  preferences storage.
- [Coil](https://github.com/coil-kt/coil) for asynchronous loading of images.
- [Accompanist](https://github.com/google/accompanist) for the handling of user permissions in
  Jetpack Compose.
- [Vico](https://github.com/patrykandpatrick/vico) for charts.
- [Lottie Compose](https://github.com/airbnb/lottie-android) for animations.

#### Pull Request Process

1. **Create a Branch:** Create a new branch from `main`
    - Name it descriptively: `feature/add-dark-mode` or `fix/crash-on-login`.
2. **Code Style:** Ensure your code adheres to
   the [official Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide).
3. **Testing:**
    - Run Unit Tests: `./gradlew test`
    - Run Instrumented Tests: `./gradlew connectedAndroidTest` (requires emulator/device)
4. **Commit Messages:** Write clear, concise commit messages
   following [Conventional Commits](https://www.conventionalcommits.org/)
    - Good: `fix: resolve crash on settings screen (fixes #123)`
    - Bad: `fixed bug`
5. **Push:** Push to your fork.
6. **Open PR:** Submit a Pull Request to the `main` branch of the original repository and describe
   your changes and/or improvements.
7. **Review:** A maintainer will review your code. Be open to feedback! We may ask for changes to
   match the project style or architecture.

### Translations

*Coming soon...*

We want LibreFit to be accessible to everyone!

- We will eventually use **Weblate** for translations.
- Please **do not** submit Pull Requests for `strings.xml` files directly, as they will likely be
  overwritten by the translation platform sync.
- Join our translation project here: _Not available yet_.

## Attribution

This guide is based on the [contributing.md](https://contributing.md/generator)!