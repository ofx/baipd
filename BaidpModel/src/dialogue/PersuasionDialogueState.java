package dialogue;

public enum PersuasionDialogueState {

    Unopened,  // The dialogue is unopened when the topic is not yet determined, once the proponent of the dialogue has played its first claim
               // the dialogue state switches to active.
    Active,    // The dialogue is active when the topic has been determined and the participants are playing, once the termination condition
               // has been met, the state switches to terminated.
    Terminated // The dialogue is terminated.

}
