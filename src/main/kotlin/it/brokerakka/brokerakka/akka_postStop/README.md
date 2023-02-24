# Explanation

Try to use the `PostStop` signal sent by an actor on stopping

The ordering on stopping is strict:
All PostStop signals of the children are processed before PostStop signal of the parent is processed.