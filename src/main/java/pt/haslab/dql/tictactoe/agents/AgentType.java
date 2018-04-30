package pt.haslab.dql.tictactoe.agents;

public enum AgentType {
    HUMAN("human"),
    BASIC("basic"),
    MYIAGI("mrmiyagi");

    private final String desc;

    private AgentType(String l){
        this.desc = l;
    }

    @Override
    public String toString() {
        return this.desc;
    }


}
