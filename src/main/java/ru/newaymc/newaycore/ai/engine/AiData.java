package ru.newaymc.newaycore.ai.engine;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class AiData implements Serializable {
    @Serial
    private static final long serialVersionUID = -3774946055521111215L;

    private int state;
    private boolean allowAttack;
    private boolean seeTarget;

    private boolean canFindCover;
    private boolean canBorderPatrol;
    private boolean canSimpleFormation;
    /**
     *
     * @param state 1 = NORMAL; 2 = ALERTED; 3 = IN BATTLE;
     * @param allowAttack
     * @param seeTarget
     * @param canFindCover
     * @param canBorderPatrol
     * @param canSimpleFormation
     */
    public AiData(int state, boolean allowAttack, boolean seeTarget, boolean canFindCover, boolean canBorderPatrol, boolean canSimpleFormation) {
        this.state = state;
        this.allowAttack = allowAttack;
        this.seeTarget = seeTarget;
        this.canFindCover = canFindCover;
        this.canBorderPatrol = canBorderPatrol;
        this.canSimpleFormation = canSimpleFormation;
    }

    @Override
    public String toString() {
        return "AiData {" +
                "state=" + state +
                ", allowAttack=" + allowAttack +
                ", seeTarget=" + seeTarget +
                ", canFindCover=" + canFindCover +
                ", canBorderPatrol=" + canBorderPatrol +
                ", canSimpleFormation=" + canSimpleFormation +
                '}';
    }
}
