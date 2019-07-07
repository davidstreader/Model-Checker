package mc.processmodels;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Object;
import com.rits.cloning.Cloner;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import mc.exceptions.CompilationException;

public abstract class ProcessModelObject implements Serializable {

  // fields
  @Getter
  @Setter
  private String id;  // name
  @Getter
  private String type;

  public ProcessModelObject(String id, String type) {
    this.id = id;
    this.type = type;

  }

  public ProcessModelObject copy() throws CompilationException {
    Cloner cloner = new Cloner();
    cloner.dontClone(Context.class);
    cloner.dontClone(Z3Object.class);
    cloner.dontClone(Expr.class);
    cloner.dontClone(BoolExpr.class);
    return cloner.deepClone(this);
  }
}
