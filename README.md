# SkillSync frontend

A responsive React prototype for the SkillSync peer learning and skill recommendation platform. It includes every learner screen in the supplied design guide, plus a public landing page and an illustrative admin overview. Video calls/streaming are intentionally deferred to a later product milestone.

## Start the app

```sh
npm install
npm run dev
```

The demo login accepts any valid email address and a password with at least six characters. Sign-up continues through the skill profile onboarding. Profile, connection and message data are stored in the current browser's `localStorage`; use **Settings → Reset demo data** to restore the seeded example. The light/dark theme choice is stored separately and stays selected after refresh.

## Architecture and data flow

`App.jsx` owns the active screen, signed-in demo session, selected peer, and app-wide success/error feedback. Page-local details (search terms, filters, form drafts, selected conversation) stay inside each page. The shared `useWorkspace` hook owns initial loading, refresh, mutation results, and request errors. Pages call actions passed down by the app; they do not access browser storage or make network requests directly.

`services/demoApi.js` is an asynchronous demo service boundary. It reads and writes a versioned local storage record, validates demo credentials and messages, and returns structured data. Replace this module's implementations with `fetch` calls when the backend is ready while preserving its method results. Keep server-side validation and authorization authoritative; the demo's browser validation is only for interaction feedback. `data/seed.js` is illustrative content, not a source of real recommendation scores.

## Screen and component map

| Screen/component | Purpose and inputs | State and events | Communication |
| --- | --- | --- | --- |
| `Landing` | Explain the product and invite account creation | No local state; handles login/signup clicks | Sends the chosen intent to `App` |
| `AuthPage` | Validate demo login and signup forms | Name, email, password, form feedback; submits or changes mode | Calls `App.authenticate` |
| `Onboarding` | Capture skills to teach/learn and a learning goal | Editable skill lists and goal; add/remove/finish/skip | Sends profile updates to the demo service via `App` |
| `Dashboard` | Show progress, activity and starter peer matches | No persistent state; navigation and match actions | Opens profile, discovery, recommendations or match details |
| `Profile` | View and edit learner identity and skills | Draft fields and edit mode; save/cancel | Sends validated profile updates to `App` |
| `Discover` | Search peers and filter by skill, schedule and level | Query and filters; view/connect/clear | Reads shared peer data and sends selected peer/actions upward |
| `Recommendations` | Explain and browse peer suggestions | No local state; opens details/connects | Uses the same peer collection as discovery |
| `RecommendationDetail` | Explain the profile signals behind one suggestion | No local state; back/connect/message | Receives the selected peer and current user as props |
| `Connections` | Separate pending requests from connected learners | No local state; message/discover/accept | Updates peer status through `App` |
| `Messages` | Read and send demo peer conversations | Selected conversation and message draft; submit | Reads shared messages and sends new messages through `App` |
| `SkillAnalysis` | Summarize learning goals and useful next steps | No local state; edit goals/find a partner | Navigates to profile or discovery |
| `Settings` | Edit account details, preferences and demo data | Form drafts and preference toggles; save/reset | Updates profile or resets demo store via `App` |
| `Admin` | Preview a separate-role admin concept | No local state | Uses illustrative aggregate data; real access control belongs to the backend |
| `AppShell` | Provide desktop sidebar, mobile navigation and top bar | Receives page/user and navigation callbacks | Routes user actions back to `App` |
| Shared UI (`ui.jsx`) | Standardize buttons, avatars, tags, fields, status and feedback | Stateless presentational components | Props in; callbacks out |

## Folder responsibilities

- `src/components/`: reusable shell and presentation components.
- `src/pages/`: screen-level components composed from shared UI.
- `src/services/`: async boundary for demo/backend data operations.
- `src/hooks/`: loading and mutation lifecycle shared across screens.
- `src/data/`: resettable example records.
- `src/styles.css`: design tokens, layout, responsive breakpoints and component styles.

## Backend integration seam

The first backend merge should replace `demoApi.load`, `authenticate`, `updateUser`, `setPeerStatus`, `sendMessage` and `reset` with API calls. The frontend expects a workspace object containing `user`, `peers`, `messages`, `activities` and `loggedIn`. Recommendation explanations should be returned as structured reason fields by the recommendation service. Compatibility scores in the seed records are demo-only.
