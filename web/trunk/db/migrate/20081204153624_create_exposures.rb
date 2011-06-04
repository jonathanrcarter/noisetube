class CreateExposures < ActiveRecord::Migration
  def self.up
    create_table :exposures do |t|
      t.float :avg
      t.float :min
      t.float :max

      t.timestamps
      add_column :tracks , :exposure_id, :integer
    end
  end

  def self.down
    remove_column :tracks , :exposure_id
    drop_table :exposures
  end
end
