# Explanation

Example of akka supervision strategy

We can see that after failure, the supervised actor is stopped and immediately restarted.
We can also see a log entry reporting the exception that was handled in thi case.

We also used in this example the PreRestart signal.