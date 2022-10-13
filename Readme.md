# Before You Die

# Concept

Task organization with dependency chains.  Kinda like google buganizer or jira (task management applications) but simple and for a todo list.

# Example

Imagine you need to create a bank account and you're immigrating to a new country.

You have a dependency graph of things to do in order to achieve that goal like so:

```
Make a Government ID -> Make a Cell Phone Number ----
                                                    \ 
                                                     \
Aquire Proof of employment   ------------------------>  Make a Bank Account
```

The creation of a bank account is somewhat contrived, but perhaps planning a vacation is more your style.  
You can imagine you need to select dates that match everyone's schedule and events you wish to attend, secure a hotel 
and flight, purchase various tickets, arrange for visas etc in some particular order.

Rather than represent this chain of events as a flat list (or even a simple list with indented items),
you can represent it as this kind of "graph" where one task points to the tasks it blocks (one or more).

Now you can have a special list that only shows you the tasks that are currently not blocked so you can get things done 
without the stress of ***all*** the things you need to do bogging you down.  

Just open up the list and start addressing things you can do *right now* without the overhead