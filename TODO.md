# Requisite functionality
* [ ] FTS in order to have the ability to add blocking/blocked/parent/children tasks from edit view
  * [ ] FTS can also be used to search for notes from GraphView
* [ ] Drag to reorder
  * [ ] some way to represent ordering in list view (aka Rank) [JIRA's lexorank](https://github.com/xissy/lexorank) [stackoverflow](https://softwareengineering.stackexchange.com/questions/195308/storing-a-re-orderable-list-in-a-database, )
* [x] Compose based UI for desktop/web/android
  * [x] List View
    * [ ] Whether blocked or not
    * [x] Completed expandable gutter
  * [ ] Graph view (should I roll this? Use graphviz as an SVG generator? How can I make it all clickable then)
* [ ] Syncing to a database
  * [ ] Self-hosted
  * [ ] Cloud based solution that is easy to spin up/sign up
  * [ ] Firebase solution (this would actually replace sqlite altogether)
* [ ] (optionally if compose doesn't work) SwiftUI based UI for iOS

# High Level Ideas
* [ ] Multiple Lists/Graphs that can be chosen between (like separate keep notes). I can do this with an extra column in the db for list id, and a list table for names
* [ ] Multiview mode that merges all the lists into one actionable items list

# Documentation
* [ ] A better graph structure example in the readme, perhaps a screenshot

# Stretches
* [ ] Periodic database backup
  * Snapshots
  * File copy
* [ ] Undo functionality, can use the "modify" table that is used for syncing, always keep it a minimum lenght dont drop after syncing.
  * Might necessitate a "redo" table as well that is the mirror of undo.
* [ ] spellfix1 integration in order to have fuzzy text search
* [ ] Inline markdown in the description field
* [ ] Switch description to a more efficient data structure like a rope.
  * For compose this might require wrapping it in a data class that keeps a version for comparison and recomposition,
    but an internally mutable rope
