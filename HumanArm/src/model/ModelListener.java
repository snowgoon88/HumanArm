/**
 * 
 */
package model;

/**
 * Generic Model Listener to model of class 'M'.
 * As explained in http://onjava.com/pub/a/onjava/2004/07/07/genericmvc.html
 * 
 * @author alain.dutech@loria.fr
 *
 */
public interface ModelListener <M>
{
  void modelChanged (M model);
}