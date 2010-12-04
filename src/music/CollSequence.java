/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package music;

/**
 *
 * @author Ilya
 */
public interface CollSequence<T> {

  public int getNumCombos();

  public T getCombo(int index);

  int getHeur();
}
