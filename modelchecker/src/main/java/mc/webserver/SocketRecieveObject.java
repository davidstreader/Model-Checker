package mc.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by sanjay on 18/01/2017.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SocketRecieveObject {
  Object context;
  Object ast;
}
