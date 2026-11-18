# Spinner, ArrayAdapter and Meaningful Selection

Android lab implementation demonstrating Spinner with ArrayAdapter, stable ID selection, custom layouts, and state restoration.

## Features

1. **Spinner with ArrayAdapter** - Mushroom types and Collection points using custom adapters
2. **Prompt as non-selectable item** - Clear initial prompt that cannot be submitted as a valid choice
3. **Stable ID selection** - Selection stored by domain object ID (scientific name), not position
4. **Custom row & dropdown layouts** - Different layouts for closed/expanded states
5. **Independent checkbox preference** - Checkbox in dropdown for non-sensitive preference (edible/collected) that doesn't affect single-choice selection
6. **Rotation persistence** - Selected value restored after configuration change via stable ID
7. **Empty list handling** - Graceful empty state display
8. **List reordering resilience** - Selection survives list reordering because it's ID-based

## Architecture

```
Source Collection (List<MushroomType>) 
    → MushroomAdapter (transforms domain objects to Views)
        → Spinner (displays selected item + dropdown)
            → Selection stored by stable ID
```

## Key Classes

- `MushroomType` / `CollectionPoint` - Domain models with stable IDs (Parcelable)
- `MushroomAdapter` / `CollectionPointAdapter` - Custom ArrayAdapters with:
  - Custom row layout (closed state)
  - Custom dropdown layout (expanded state with checkbox)
  - Stable IDs via `getItemId()` returning domain object ID hash
  - Prompt handling at position 0
- `MainActivity` - Demonstrates all features with explanation panel

## Lab Requirements Covered

| Requirement | Implementation |
|-------------|----------------|
| Spinner with ArrayAdapter + prompt | ✅ MushroomAdapter/CollectionPointAdapter with null prompt at position 0 |
| Selection handling with stable ID | ✅ `getItemId()` returns domain ID hash; `hasStableIds()` returns true |
| Custom row + dropdown layout | ✅ `spinner_row_*.xml` + `spinner_dropdown_*.xml` |
| Checkbox independent of selection | ✅ Checkbox in dropdown only, toggles preference without changing selection |
| Rotation restoration | ✅ `onSaveInstanceState`/`onRestoreInstanceState` with stable ID |
| Empty list state | ✅ `tvEmptyState` TextView shown when list is empty |

## Build

```bash
./gradlew assembleDebug
```

## Testing the Features

1. **Select a mushroom/collection point** - See confirmation card update with stable ID
2. **Rotate device** - Selection persists via stable ID
3. **Switch modes** - Button toggles between Mushroom Types and Collection Points
4. **Dropdown checkbox** - Toggle "edible"/"collected" - doesn't change Spinner selection
5. **Empty list** - Call `simulateEmptyList()` to test empty state
6. **Reorder list** - Call `simulateReorderList()` to verify ID-based selection survives

## Explanation of Key Concepts

### Why Adapter > Data Copy
The adapter:
- Recycles views (convertView pattern)
- Binds domain data to specific layouts
- Manages prompt separately from valid choices
- Provides stable IDs for reliable selection tracking

### Prompt vs Choice
- Position 0 = prompt (returns `AdapterView.NO_ID`)
- Prompt disabled in dropdown (alpha 0.5, not clickable)
- Validation checks `position > 0 && id != NO_ID`

### Stable IDs Survive Reordering
If list order changes, position-based selection breaks. Stable IDs (domain object ID) survive because:
- `getItemId(position)` returns `item.getId().hashCode()`
- `hasStableIds()` returns `true`
- Selection restored by finding item with matching ID

### Checkbox Independence
- Spinner = single choice (mutually exclusive, radio-like)
- Checkbox = per-item preference (can toggle multiple independently)
- Checkbox in dropdown only, hidden in closed state
